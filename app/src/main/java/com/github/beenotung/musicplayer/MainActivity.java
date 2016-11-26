package com.github.beenotung.musicplayer;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.github.beenotung.musicplayer.Playlist.playlist;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static MainActivity mainActivity;

    private FloatingActionButton fab;
    private final String TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeContainer == null)
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                else
                    activeContainer.onFabClicked();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initVar();
        switchNavItem(R.id.nav_player);
    }

    LayoutInflater mInflater;
    Settings settings;

    void initVar() {
        folderSharedPreferences = getSharedPreferences(FolderContainer.class.getName(), Context.MODE_PRIVATE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        settings = new Settings();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final int[] containerIds = {R.id.container_folder, R.id.container_player, R.id.container_settings};
    private final View[] containerView = new View[containerIds.length];
    private final HashMap<Integer, Container> containers = new HashMap<>();

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switchNavItem(item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void switchNavItem(int navId) {
        // Handle navigation view item clicks here.
        final int containerId;
        Utils.Supplier<Container> containerSupplier = null;

        if (navId == R.id.nav_folder) {
            setTitle(R.string.folder);
            containerId = R.id.container_folder;
            containerSupplier = new Utils.Supplier<Container>() {
                @Override
                public Container apply() {
                    return new FolderContainer(findViewById(containerId));
                }
            };
        } else if (navId == R.id.nav_player) {
            setTitle(R.string.player);
            containerId = R.id.container_player;
            containerSupplier = new Utils.Supplier<Container>() {
                @Override
                public Container apply() {
                    return new PlayerContainer(findViewById(containerId));
                }
            };
        } else if (navId == R.id.nav_settings) {
            setTitle(R.string.settings);
            containerId = R.id.container_settings;
            containerSupplier = new Utils.Supplier<Container>() {
                @Override
                public Container apply() {
                    return new SettingsContainer(findViewById(containerId));
                }
            };
        } else if (navId == R.id.nav_about) {
            setTitle(R.string.feedback);
            containerId = 0;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beenotung/music-player/blob/master/README.md"));
            startActivity(browserIntent);
        } else if (navId == R.id.nav_share) {
            setTitle(R.string.share);
            containerId = 0;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://github.com/beenotung/music-player");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (navId == R.id.nav_feedback) {
            setTitle(R.string.feedback);
            containerId = 0;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/beenotung/music-player/issues"));
            startActivity(browserIntent);
        } else {
            containerId = R.id.container_player;
        }

        Container container = null;
        if (containers.get(containerId) == null) {
            if (containerSupplier != null) {
                container = containerSupplier.apply();
                if (container != null) {
                    containers.put(containerId, container);
                }
            }
        } else {
            container = containers.get(containerId);
        }

        for (int i : containerIds) {
            View view = findViewById(i);
            if (view == null)
                continue;
            if (i == containerId) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
            Container c = containers.get(containerId);
            if (c != null) {
                if (c == container) {
                    Log.d(TAG, "show this" + c);
                    c.onEnter();
                } else {
                    c.onLeave();
                }
            }
        }
    }


    Container activeContainer;

    abstract class Container {
        public final View view;

        public Container(View view) {
            this.view = view;
        }

        void onEnter() {
            activeContainer = this;
        }

        void onLeave() {
            if (activeContainer == this)
                activeContainer = null;
        }

        void onFabClicked() {
        }
    }

    synchronized int newUid() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int res = preferences.getInt("uid", 0) + 1;
        preferences.edit()
                .putInt("uid", res)
                .apply();
        return res;
    }

    final int MY_PERMISSIONS_REQUEST_ACCESS_FILE = 15;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FILE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activeContainer.onEnter();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    void grant_permission(String permission) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        MY_PERMISSIONS_REQUEST_ACCESS_FILE);

                // MY_PERMISSIONS_REQUEST_ACCESS_FILE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    SharedPreferences folderSharedPreferences;

    ArrayList<Folder> getUserFolders() {
        ArrayList<Folder> res = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(folderSharedPreferences.getString("list", "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Folder folder = new Folder();
                folder.title = jsonObject.getString("title");
                folder.path = jsonObject.getString("path");
                folder.id = jsonObject.getInt("id");
                res.add(folder);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    class FolderContainer extends Container {
        ListView listView;
        ArrayList<Folder> folders = new ArrayList<>();
        private Drawable oriDrawable;
        private Drawable normal_fab_drawable;
        private Drawable select_fab_drawable;
        private final FolderAdapter adapter;

        class FolderAdapter extends BaseAdapter {

            FolderAdapter() {
            }

            @Override
            public int getCount() {
                return folders.size();
            }

            @Override
            public Folder getItem(int position) {
                return folders.get(position);
            }

            @Override
            public long getItemId(int position) {
                return folders.get(position).id;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.listitem_folder, null);
                }
                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
                TextView tv_title = (TextView) convertView.findViewById(R.id.folder_title);
                TextView tv_path = (TextView) convertView.findViewById(R.id.folder_path);
                if (tv_title == null
                        || tv_path == null
                        || checkBox == null
                        ) {
                    throw new IllegalStateException("Invalid list item");
                }
                final Folder folder = getItem(position);
                tv_title.setText(folder.title);
                tv_path.setText(folder.path);
                if (checkBox.isChecked())
                    checkBox.performClick();
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        folder.checked = isChecked;
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Folder folder = getItem(position);
                        Snackbar.make(view, folder.title, Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                        if (mode == MODE_SELECT) {
                            if (position == 0) {
                            /* go back to parent */
                                if (systemFolderParent != null && systemFolderParent.getParentFile() != null) {
                                    systemFolderParent = systemFolderParent.getParentFile();
                                } else {
                                    systemFolderParent = null;
                                }
                            } else {
                            /* go to child */
                                systemFolderParent = getItem(position).file();
                            }
                            showSystemFolders();
                        }
                    }
                });
                return convertView;
            }
        }

        public FolderContainer(View view) {
            super(view);
            listView = (ListView) view.findViewById(R.id.container_folder);
            if (listView == null) {
                throw new IllegalStateException("folder list view not found");
            }
            adapter = new FolderAdapter();
            listView.setAdapter(adapter);
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        void onEnter() {
            super.onEnter();
            oriDrawable = fab.getDrawable();
            normal_fab_drawable = getDrawable(R.drawable.ic_menu_gallery);
            select_fab_drawable = getDrawable(android.R.drawable.checkbox_on_background);
            fab.setImageDrawable(normal_fab_drawable);
            showUserFolders();
        }


        void showUserFolders() {
            folders.clear();
            folders.addAll(getUserFolders());
            adapter.notifyDataSetChanged();
        }

        File systemFolderParent;

        void showSystemFolders() {
            grant_permission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (systemFolderParent == null) {
                folders.clear();
                folders.add(new Folder(0, Environment.getRootDirectory()));
                folders.add(new Folder(1, Environment.getDataDirectory()));
                folders.add(new Folder(2, Environment.getDownloadCacheDirectory()));
                folders.add(new Folder(3, Environment.getExternalStorageDirectory()));
                folders.add(new Folder(4, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
                folders.add(new Folder(4, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)));
                folders.add(new Folder(4, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
                folders.add(new Folder(4, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
                Collections.sort(folders, new Comparator<Folder>() {
                    @Override
                    public int compare(Folder lhs, Folder rhs) {
                        return lhs.title.compareTo(rhs.title);
                    }
                });
            } else {
                File[] files = systemFolderParent.listFiles();
                if (files == null) {
                    Snackbar.make(view, "Empty Folder", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                folders.clear();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        folders.add(new Folder(i, files[i]));
                    }
                }
                Collections.sort(folders, new Comparator<Folder>() {
                    @Override
                    public int compare(Folder lhs, Folder rhs) {
//                        return lhs.title.compareTo(rhs.title);
                        return -Long.compare(lhs.size(), rhs.size());
                    }
                });
                folders.add(0, new Folder(-1, systemFolderParent));
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        void onLeave() {
            fab.setImageDrawable(oriDrawable);
            super.onLeave();
        }

        final byte MODE_NORMAL = 1;
        final byte MODE_SELECT = 2;
        byte mode = MODE_NORMAL;

        @Override
        void onFabClicked() {
            super.onFabClicked();
            if (mode == MODE_NORMAL) {
                mode = MODE_SELECT;
                Snackbar.make(view, "Select a folder", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                fab.setImageDrawable(select_fab_drawable);
                listView.setBackgroundColor(R.color.colorFileExplorerBG);
                systemFolderParent = null;
                showSystemFolders();
            } else {
                mode = MODE_NORMAL;
                fab.setImageDrawable(normal_fab_drawable);
                listView.setBackgroundColor(android.R.color.white);
                ArrayList<Folder> res = new ArrayList<>();
                for (Folder folder : folders) {
                    if (folder.checked) {
                        res.add(folder);
                    }
                }
                res.addAll(getUserFolders());
                JSONArray jsonArray = new JSONArray();
                for (Folder re : res) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", re.id);
                        jsonObject.put("title", re.title);
                        jsonObject.put("path", re.path);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                folderSharedPreferences.edit().putString("list", jsonArray.toString()).apply();
                showUserFolders();
            }
        }
    }

    public class PlayerContainer extends Container implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
        final String TAG = getClass().getName();
        private Drawable oriDrawable;
        PlayerService playerService;
        private final ImageButton btn_toggle;
        private final TextView tv_title;
        private final TextView tv_path;
        private MediaPlayer mediaPlayer;
        private final ListView listView;

        public PlayerContainer(View view) {
            super(view);
            final ImageButton btn_prev = (ImageButton) view.findViewById(R.id.btn_prev);
            btn_toggle = (ImageButton) view.findViewById(R.id.btn_play);
            final ImageButton btn_next = (ImageButton) view.findViewById(R.id.btn_next);
            tv_title = (TextView) view.findViewById(R.id.title);
            tv_path = (TextView) view.findViewById(R.id.path);
            listView = (ListView) view.findViewById(R.id.listview_song_list);
            if (Utils.hasNull(btn_prev
                    , btn_toggle
                    , btn_next
                    , listView
                    , tv_title
                    , tv_path
            )) {
                throw new IllegalStateException("Invalid View");
            }
            btn_toggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle_play();
                }
            });
            btn_prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prev_song();
                }
            });
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    next_song();
                }
            });
            adapter = new PlayerAdapter();
            listView.setAdapter(adapter);
            tv_title.setText(getString(R.string.waiting_media_player_service));
            tv_path.setText("");
            Log.d(TAG, "wait for service");
            btn_next.setEnabled(false);
            btn_prev.setEnabled(false);
            btn_toggle.setEnabled(false);
            ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "got service");
                    playerService = ((PlayerService.PlayerBinder) service).getService();
                    mediaPlayer = playerService.mediaPlayer;

                    tv_title.setText(getString(R.string.ready));
                    btn_next.setEnabled(true);
                    btn_prev.setEnabled(true);
                    btn_toggle.setEnabled(true);

                    mediaPlayer.setOnErrorListener(PlayerContainer.this);
                    mediaPlayer.setOnCompletionListener(PlayerContainer.this);
                    mediaPlayer.setOnPreparedListener(PlayerContainer.this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "failed to get service");
                    playerService = null;
                }
            };
            Intent intent = new Intent(MainActivity.this, PlayerService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }


        int lastIdx = -1;

        void toggle_play() {
            if (playerService.isPlaying()) {
                pause();
            } else {
                play();
            }
        }

        void play() {
            if (playerService.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            grant_permission(Manifest.permission.WAKE_LOCK);
            grant_permission(Manifest.permission.MEDIA_CONTENT_CONTROL);
            if (lastIdx == playlist.idx()) {
                resume();
            } else {
                for (; ; ) {
                    try {
                        playerService.mediaPlayer.setDataSource(playlist.currentSongPath());
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (playlist.songs.size() > 1) {
                            playlist.idx(playlist.idx() + 1);
                        } else {
                            break;
                        }
                    }
                }
                playerService.mediaPlayer.prepareAsync();
                lastIdx = playlist.idx();
            }
            tv_title.setText(playlist.currentSongName());
            tv_path.setText(getString(R.string.playing));
            btn_toggle.setBackgroundResource(android.R.drawable.ic_media_pause);
        }

        void resume() {
            grant_permission(Manifest.permission.MEDIA_CONTENT_CONTROL);
            mediaPlayer.start();
            tv_path.setText(R.string.player);
            btn_toggle.setBackgroundResource(android.R.drawable.ic_media_pause);
        }

        void pause() {
            grant_permission(Manifest.permission.MEDIA_CONTENT_CONTROL);
            if (playerService.isPlaying()) {
                mediaPlayer.pause();
            }
            tv_path.setText(getString(R.string.paused));
            btn_toggle.setBackgroundResource(android.R.drawable.ic_media_play);
        }

        synchronized void prev_song() {
            if (settings.is_random_order()) {
                playlist.idx(lastIdx);
            } else {
                playlist.idx(playlist.idx() - 1);
            }
            play();
        }

        synchronized void next_song() {
            if (settings.is_random_order()) {
                playlist.idx(new Random().nextInt());
            } else {
                playlist.idx(playlist.idx() + 1);
            }
            play();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.stop();
            if (mp != mediaPlayer) {
                Log.d(TAG, "set to next track");
                mediaPlayer.setNextMediaPlayer(mp);
            }
            if (settings.is_auto_play()) {
                next_song();
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mp.reset();
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }

        class PlayerAdapter extends BaseAdapter {
            @Override
            public int getCount() {
                return playlist.songs.size();
            }

            @Override
            public Playlist.Song getItem(int position) {
                return playlist.songs.get(position);
            }

            @Override
            public long getItemId(int position) {
                try {
                    return playlist.songs.get(position).id();
                } catch (NoSuchAlgorithmException | IOException e) {
                    e.printStackTrace();
                    return position;
                }
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.listitem_folder, null);
                }
                final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
                final TextView tv_title = (TextView) convertView.findViewById(R.id.folder_title);
                TextView tv_path = (TextView) convertView.findViewById(R.id.folder_path);
                final LinearLayout container = (LinearLayout) convertView.findViewById(R.id.container_listitem);
                if (Utils.hasNull(
                        checkBox
                        , tv_title
                        , tv_path
                        , container
                )) {
                    throw new IllegalStateException("Invalid list item");
                }
                final Playlist.Song song = getItem(position);
                tv_title.setText(song.name);
                Uri uri = Uri.parse(song.path);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(MainActivity.this, uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int sec = Integer.parseInt(durationStr) / 1000;
                StringBuilder duration = new StringBuilder();
                if (sec > 60) {
                    duration.append(sec / 60);
                    duration.append(" min. ");
                    sec = sec % 60;
                }
                duration.append(sec);
                duration.append(" sec.");
                tv_path.setText(duration.toString());
                checkBox.setChecked(song.isSelected);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            song.isSelected = true;
                        }
                    }
                });
//                tv_title.setTextColor(position == playlist.idx() ? R.color.playing : R.color.not_playing);
//                container.setBackgroundColor(position == playlist.idx() ? R.color.bg_playing : R.color.bg_not_playing);
//                if (position == playlist.idx()) {
//                    Log.d(TAG, "playing this: " + song.name);
//                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position == playlist.idx()) {
                            return;
                        }
                        playlist.idx(position);
                        play();
                    }
                });
                return convertView;
            }
        }

        PlayerAdapter adapter;

        @Override
        void onEnter() {
            super.onEnter();
            oriDrawable = fab.getDrawable();
            fab.setImageDrawable(getDrawable(android.R.drawable.ic_menu_delete));
            if (playlist.songs.isEmpty()) {
//                playlist.scanSongs(getUserFolders(), getPreferences(Context.MODE_PRIVATE));
                playlist.scanSongs(getUserFolders());
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        void onLeave() {
            super.onLeave();
            fab.setImageDrawable(oriDrawable);
        }

        @Override
        void onFabClicked() {
            super.onFabClicked();
            Snackbar.make(view, "Removed From List", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            ArrayList<Playlist.Song> xs = new ArrayList<>();
            for (Playlist.Song song : playlist.songs) {
                if (song.isSelected) {
                    song.hide = true;
                } else {
                    xs.add(song);
                }
            }
            /* TODO store hide option */
            playlist.songs.clear();
            adapter.notifyDataSetChanged();
            playlist.songs = xs;
            adapter.notifyDataSetChanged();
        }
    }

    class Settings {
        SharedPreferences settingsSharedPreferences = getSharedPreferences(SettingsContainer.class.getName(), Context.MODE_PRIVATE);

        boolean is_auto_play() {
            return settings.settingsSharedPreferences.getBoolean("auto_play", true);
        }

        boolean is_random_order() {
            return settings.settingsSharedPreferences.getBoolean("random_order", false);
        }
    }


    class SettingsContainer extends Container {
        private final CheckBox cb_auto_play;
        private final CheckBox cb_random_order;

        public SettingsContainer(View view) {
            super(view);
            cb_auto_play = (CheckBox) view.findViewById(R.id.cb_auto_play);
            cb_random_order = (CheckBox) view.findViewById(R.id.cb_random_order);
            if (Utils.hasNull(
                    cb_auto_play
                    , cb_random_order
            )) {
                throw new IllegalStateException("Invalid layout");
            }
            cb_auto_play.setChecked(settings.is_auto_play());
            cb_random_order.setChecked(settings.is_random_order());
        }

        @Override
        void onLeave() {
            super.onLeave();
            settings.settingsSharedPreferences.edit()
                    .putBoolean("auto_play", cb_auto_play.isChecked())
                    .putBoolean("random_order", cb_random_order.isChecked())
                    .apply();
        }
    }

    class AboutContainer extends Container {

        private final WebView webView;

        public AboutContainer(View view) {
            super(view);
            webView = (WebView) view.findViewById(R.id.webview_about);
            grant_permission(Manifest.permission.INTERNET);
            webView.loadUrl("https://github.com/beenotung/music-player/blob/master/README.md");
        }

    }
}
