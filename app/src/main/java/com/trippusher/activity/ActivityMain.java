package com.trippusher.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;
import com.trippusher.fragment.FragmentAddTrip;
import com.trippusher.fragment.FragmentBaseTripAds;
import com.trippusher.fragment.FragmentMessage;
import com.trippusher.fragment.FragmentMyTrip;
import com.trippusher.fragment.FragmentNotification;
import com.trippusher.fragment.FragmentSearch;
import com.trippusher.fragment.FragmentSetting;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends AppCompatActivity {
    Fragment fragment;
    FragmentManager fragmentManager;
    private List<Menu> MenuList = new ArrayList<>();
    ListView mDrawerList;
    MenuAdapter adapter;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String EditMyTrips, EditNotification, AirlineId, ChangeProfile, UserId, ChatBack, password, resultBase;
    JSONParser jsonParser = new JSONParser();
    DrawerLayout drawer;
    Toolbar toolbar;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        AirlineId = prefs.getString("airlineId", null);
        UserId = prefs.getString("userId", null);
        resultBase = prefs.getString("resultBase", null);
        password = prefs.getString("password", null);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            EditMyTrips = bundle.getString("EditMyTrips");
            ChangeProfile = bundle.getString("ChangeProfile");
            EditNotification = bundle.getString("EditNotification");
        }
        ChatBack = prefs.getString("ChatBack", null);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (AppStatus.getInstance(ActivityMain.this).isOnline()) {
            if (resultBase == null) {
                new AsyncTask<String, String, JSONObject>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected JSONObject doInBackground(String... args) {
                        ArrayList parametarGetAirport = new ArrayList();
                        parametarGetAirport.add(new BasicNameValuePair("status_id", "1"));
                        parametarGetAirport.add(new BasicNameValuePair("airline_id", AirlineId));
                        JSONObject json = jsonParser.makeHttpRequest(AppStatus.getInstance(ActivityMain.this).baseurl() + "get_airports", "POST", parametarGetAirport);
                        return json;
                    }

                    protected void onPostExecute(JSONObject result) {
                        try {
                            if (result != null) {
                                int status_id = result.getInt("status_id");
                                String MSG = result.getString("status_msg");
                                if (status_id != 0) {
                                    resultBase = result.getString("data");
                                    editor.putString("resultBase", resultBase);
                                    editor.commit();
                                } else {
                                    Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.execute();
            }
        }
        MenuList.add(new Menu("1", "Custom Search", 0, 0));
        MenuList.add(new Menu("2", "Post Trip", 0, 0));
        MenuList.add(new Menu("3", "Base Trip Ads", 0, 0));
        MenuList.add(new Menu("4", "Message", 0, 0));
        MenuList.add(new Menu("5", "Notification", 0, 0));
        MenuList.add(new Menu("6", "Setting", 0, 0));
        MenuList.add(new Menu("7", "My Trip", 0, 0));
        MenuList.add(new Menu("8", "Logout", 0, 0));
        adapter = new MenuAdapter(this, MenuList);
        mDrawerList.setAdapter(adapter);
        if (ChatBack != null) {
            editor.putString("ChatBack", null);
            editor.commit();
            fragment = new FragmentMessage();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else if (ChangeProfile != null) {
            fragment = new FragmentSetting();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else if (EditMyTrips != null) {
            fragment = new FragmentMyTrip();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else if (EditNotification != null) {
            fragment = new FragmentNotification();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else {
            drawer.openDrawer(GravityCompat.START);
            fragment = new FragmentBaseTripAds();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Menu item = (Menu) adapter.getItem(position);
                if (item.Title == "Custom Search") {
                    fragment = new FragmentSearch();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "Post Trip") {
                    fragment = new FragmentAddTrip();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "Base Trip Ads") {
                    fragment = new FragmentBaseTripAds();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "Message") {
                    fragment = new FragmentMessage();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "Notification") {
                    fragment = new FragmentNotification();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "Setting") {
                    fragment = new FragmentSetting();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "My Trip") {
                    fragment = new FragmentMyTrip();
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else if (item.Title == "Logout") {
                    editor.putString("username", null);
                    editor.putString("password", null);
                    editor.putString("fcm_id", null);
                    editor.putString("resultBase", null);
                    editor.commit();
                    Intent intent = new Intent(getApplication(), ActivityLogin.class);
                    startActivity(intent);
                }
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onBackPressed() {
        /*if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            //Toast.makeText(this, count++, Toast.LENGTH_SHORT).show();
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public class Menu {
        public String Id;
        public String Title;
        public int mCount;
        public int nCount;

        public Menu(String Id, String Title, int mcount, int ncount) {
            this.Id = Id;
            this.Title = Title;
            this.mCount = mcount;
            this.nCount = ncount;
        }

        public String getText() {
            return Title;
        }
    }

    private class MenuAdapter extends BaseAdapter {
        private Context context;
        private List<Menu> objects;

        public MenuAdapter(Context context, List<Menu> objects) {
            this.context = context;
            this.objects = objects;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public Object getItem(int position) {
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Menu item = (Menu) getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.items_menu, parent, false);
                holder.menu = (TextView) convertView.findViewById(R.id.txtmenu);
                holder.submenu = (TextView) convertView.findViewById(R.id.txtsubmenu);
                holder.submenu.setVisibility(View.GONE);
                if (item.Title.matches("Message")) {
                    if (item.mCount != 0) {
                        holder.submenu.setVisibility(View.VISIBLE);
                        holder.submenu.setText(item.mCount + " New");
                    }
                    holder.menu.setText(item.Title);
                } else if (item.Title == "Notification") {
                    if (item.nCount != 0) {
                        holder.submenu.setVisibility(View.VISIBLE);
                        holder.submenu.setText(item.nCount + " New");
                    }
                    holder.menu.setText(item.Title);
                } else {
                    holder.menu.setText(item.Title);
                }

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.menu.setText(item.Title.toString());
            return convertView;
        }

        class ViewHolder {
            TextView menu, submenu;
        }
    }
}
