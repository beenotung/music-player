package com.github.beenotung.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.util.ArrayList;
import java.util.HashMap;

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
            container = new FolderContainer(findViewById(containerId));
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
        String title;
        String path;
        int id;

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

    ArrayList<Folder> folders = new ArrayList<>();

    class FolderContainer extends Container {
        ListView listView;
        private SharedPreferences folderSharedPreferences;
        private final LayoutInflater mInflater;

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
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.listitem_folder, null);
                }
                TextView tv_title = (TextView) convertView.findViewById(R.id.folder_title);
                TextView tv_path = (TextView) convertView.findViewById(R.id.folder_path);
                if (tv_title == null || tv_path == null) {
                    throw new IllegalStateException("Invalid list item");
                }
                Folder folder = getItem(position);
                tv_title.setText(folder.title);
                tv_title.setText(folder.path);
                return convertView;
            }
        }

        public FolderContainer(View view) {
            super(view);
            listView = (ListView) findViewById(R.id.folder_list);
            if (listView == null) {
                throw new IllegalStateException("folder list view not found");
            }
            listView.setAdapter(new FolderAdapter());
            folderSharedPreferences = getSharedPreferences(FolderContainer.class.getName(), Context.MODE_PRIVATE);
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        void onEnter() {
            super.onEnter();
            folders.clear();
            try {
                JSONArray jsonArray = new JSONArray(folderSharedPreferences.getString("list", "[]"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Folder folder = new Folder();
                    folder.title = jsonObject.getString("title");
                    folder.path = jsonObject.getString("path");
                    folder.id = jsonObject.getInt("id");
                    folders.add(folder);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fab.setBackgroundResource(android.R.drawable.btn_plus);
        }
    }
}
