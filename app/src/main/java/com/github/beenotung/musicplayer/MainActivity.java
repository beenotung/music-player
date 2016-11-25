package com.github.beenotung.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.*;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private final int[] containerIds = {R.id.container_folder};
    private final View[] containerView = new View[containerIds.length];
    private final HashMap<Integer, Container> containers = new HashMap<>();

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int containerId = R.id.container_folder;
        Container container = null;

        if (id == R.id.nav_scan) {
            setTitle(R.string.scan);
        } else if (id == R.id.nav_folder) {
            setTitle(R.string.folder);
            containerId = R.id.container_folder;
            container = new FolderContainer(findViewById(R.id.container_folder));
        } else if (id == R.id.nav_player) {
            setTitle(R.string.player);
        } else if (id == R.id.nav_settings) {
            setTitle(R.string.settings);
        } else if (id == R.id.nav_share) {
            setTitle(R.string.share);
        } else if (id == R.id.nav_feedback) {
            setTitle(R.string.feedback);
        }

        for (int i : containerIds) {
            View view = findViewById(i);
            if (view == null)
                continue;
            if (containers.get(containerId) == null) {
                containers.put(containerId, container);
            }
            container = containers.get(containerId);
            if (container == null) {
                continue;
            }
            if (i == containerId) {
                container.onEnter();
                view.setVisibility(View.VISIBLE);
            } else {
                container.onLeave();
                view.setVisibility(View.GONE);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    class Folder {
        File file;
        String title;
        String path;
        int id;
        boolean checked = false;

        public Folder() {
        }

        public Folder(int id, File file) {
            this.file = file;
            this.id = id;
            this.path = file.getAbsolutePath();
            if (this.path.equals("/")) {
                this.title = "root";
            } else {
                this.title = file.getName();
            }
        }

        @Override
        public String toString() {
            JSONObject res = new JSONObject();
            try {
                res.put("title", title);
                res.put("path", path);
                res.put("id", id);
            } catch (JSONException e) {
                e.printStackTrace();
                onStop();
            }
            return res.toString();
        }
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

    class FolderContainer extends Container {
        ListView listView;
        ArrayList<Folder> folders = new ArrayList<>();
        private SharedPreferences folderSharedPreferences;
        private final LayoutInflater mInflater;
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
                                systemFolderParent = getItem(position).file;
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
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            folderSharedPreferences = getSharedPreferences(FolderContainer.class.getName(), Context.MODE_PRIVATE);
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
                        return lhs.title.compareTo(rhs.title);
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
}
