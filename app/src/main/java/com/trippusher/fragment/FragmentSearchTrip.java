package com.trippusher.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;
import com.trippusher.adapter.AdapterBaseTrip;
import com.trippusher.classes.TripItemList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Desktop-KS on 7/29/2017.
 */

public class FragmentSearchTrip extends Fragment {
    Fragment fragment;
    FragmentManager fragmentManager;
    Button AddNotification;
    EditText notification_title;
    TextView txtNoData;
    private RecyclerView RvBrowseAll;
    private AdapterBaseTrip Adapter;
    private List<TripItemList> searchTripItemList;
    ;
    JSONParser jsonParser = new JSONParser();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String NotificationTitle, UserId, AirlineId, NotificationTrip,
            SpnSearchBase, Date, SpnMinDays, SpnMaxDays, SpnSearchGift;
    SwipeRefreshLayout refresher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        UserId = prefs.getString("userId", null);
        AirlineId = prefs.getString("airlineId", null);
        //BaseAirportId = prefs.getString("AirportId", null);
        ///get trip without shared////
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            SpnSearchBase = bundle.getString("SpnSearchBase");
            Date = bundle.getString("Date");
            SpnMinDays = bundle.getString("SpnMinDays");
            SpnMaxDays = bundle.getString("SpnMaxDays");
            SpnSearchGift = bundle.getString("SpnSearchGift");
            NotificationTrip = bundle.getString("NotificationTrip");
        }
        ///get trip without shared////
        View rootView = inflater.inflate(R.layout.fragment_triplisting, container, false);
        RvBrowseAll = (RecyclerView) rootView.findViewById(R.id.RvBrowseall);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        RvBrowseAll.setHasFixedSize(true);
        RvBrowseAll.setLayoutManager(manager);
        refresher = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher);
        txtNoData = (TextView) rootView.findViewById(R.id.txtNotrip);
        AddNotification = (Button) rootView.findViewById(R.id.Btnfooter);
        if (NotificationTrip != null) {
            AddNotification.setVisibility(View.GONE);
        } else {
            AddNotification.setText("Add Notification");
        }

        refresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (AppStatus.getInstance(getContext()).isOnline()) {
                            SearchTripData SearchTripData = new SearchTripData();
                            SearchTripData.execute(AirlineId, SpnSearchBase);
                            refresher.setRefreshing(false);
                        } else {
                            refresher.setRefreshing(false);
                            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        if (AppStatus.getInstance(getContext()).isOnline()) {
            SearchTripData SearchTripData = new SearchTripData();
            SearchTripData.execute(AirlineId, SpnSearchBase);
        } else {
            RvBrowseAll.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
            txtNoData.setText("Please check network connection and swipe down to refresh ");
        }

        AddNotification.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                NotificationTitle = notification_title.getText().toString();
                                new AsyncTask<String, String, JSONObject>() {
                                    String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "add_search_notification";

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        ArrayList parametarGetAirline = new ArrayList();
                                        parametarGetAirline.add(new BasicNameValuePair("user_id", UserId));
                                        parametarGetAirline.add(new BasicNameValuePair("notification_title", NotificationTitle));
                                        parametarGetAirline.add(new BasicNameValuePair("gift", SpnSearchGift));
                                        parametarGetAirline.add(new BasicNameValuePair("airline_id", AirlineId));
                                        parametarGetAirline.add(new BasicNameValuePair("date", Date));
                                        parametarGetAirline.add(new BasicNameValuePair("min_days", SpnMinDays));
                                        parametarGetAirline.add(new BasicNameValuePair("max_days", SpnMaxDays));
                                        parametarGetAirline.add(new BasicNameValuePair("source_airport_id", SpnSearchBase));
                                        JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                        return json;
                                    }

                                    protected void onPostExecute(JSONObject result) {
                                        try {
                                            if (result != null) {
                                                int status_id = result.getInt("status_id");
                                                String MSG = result.getString("status_msg");
                                                if (status_id != 0) {
                                                    Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
                                                    fragment = new FragmentNotification();
                                                    fragmentManager = getActivity().getSupportFragmentManager();
                                                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                                                } else {
                                                    Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                notification_title = new EditText(getContext());
                TextView title = new TextView(getContext());
                title.setText("Notification title");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.setView(notification_title);
                alert.show();
            }
        });
        return rootView;
    }

    private class SearchTripData extends AsyncTask<String, String, JSONObject> {
        String URL = AppStatus.getbaseurl().baseurl() + "search_trips";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("airline_id", AirlineId));
            params.add(new BasicNameValuePair("gift", SpnSearchGift));
            params.add(new BasicNameValuePair("date", Date));
            params.add(new BasicNameValuePair("min_days", SpnMinDays));
            params.add(new BasicNameValuePair("max_days", SpnMaxDays));
            params.add(new BasicNameValuePair("source_airport_id", SpnSearchBase));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    int status_id = result.getInt("status_id");
                    String MSG = result.getString("status_msg");
                    if (status_id != 0) {
                        searchTripItemList= new ArrayList<>();
                        RvBrowseAll.setVisibility(View.VISIBLE);
                        txtNoData.setVisibility(View.GONE);
                        JSONArray array = new JSONArray(result.getString("data"));
                        for (int i = 0; i < array.length(); i++) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date testDate = null;
                            try {
                                testDate = sdf.parse(array.getJSONObject(i).getString("start_date"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                            String newFormat = formatter.format(testDate);
                            searchTripItemList.add(new TripItemList(
                                    array.getJSONObject(i).getString("base_airport"),
                                    array.getJSONObject(i).getString("airline_title"),
                                    array.getJSONObject(i).getString("image"),
                                    newFormat,
                                    array.getJSONObject(i).getString("flight_time_hrs") + "hrs" + " " + array.getJSONObject(i).getString("flight_time_mins") + " " + "mins",
                                    array.getJSONObject(i).getString("gift"),
                                    array.getJSONObject(i).getString("post_trip_id")));
                        }
                        if (Adapter != null) { // it works second time and later
                            Adapter.notifyDataSetChanged();
                        } else { // it works first time
                            Adapter = new AdapterBaseTrip(searchTripItemList);
                            RvBrowseAll.setAdapter(Adapter);
                        }
                    } else {
                        RvBrowseAll.setVisibility(View.GONE);
                        txtNoData.setVisibility(View.VISIBLE);
                        txtNoData.setText(MSG);
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}