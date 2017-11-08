package com.trippusher.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Date;
import java.util.List;

/**
 * Created by Desktop-KS on 7/26/2017.
 */

public class FragmentBaseTripAds extends Fragment {
    private RecyclerView RvBrowseAll;
    private AdapterBaseTrip Adapter;
    private List<TripItemList> tripItemList;
    JSONParser jsonParser = new JSONParser();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Fragment fragment;
    FragmentManager fragmentManager;
    String AirlineId, BaseAirportId, UserId;
    Button CustomSearch;
    SwipeRefreshLayout refresher;
    TextView NoTripData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        AirlineId = prefs.getString("airlineId", null);
        BaseAirportId = prefs.getString("AirportId", null);
        UserId = prefs.getString("userId", null);
        View rootView = inflater.inflate(R.layout.fragment_triplisting, container, false);
        RvBrowseAll = (RecyclerView) rootView.findViewById(R.id.RvBrowseall);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        RvBrowseAll.setHasFixedSize(true);
        RvBrowseAll.setLayoutManager(manager);
        CustomSearch = (Button) rootView.findViewById(R.id.Btnfooter);
        CustomSearch.setText("Custom Search");
        refresher = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher);
        NoTripData = (TextView) rootView.findViewById(R.id.txtNotrip);
        CustomSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fragment = new FragmentSearch();
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });
        /*if (AppStatus.getInstance(getContext()).isOnline()) {
            BaseTripData baseTripData = new BaseTripData();
            baseTripData.execute(AirlineId, BaseAirportId);
        } else {
            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
        }
        refresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (AppStatus.getInstance(getContext()).isOnline()) {
                            BaseTripData baseTripData = new BaseTripData();
                            baseTripData.execute(AirlineId, BaseAirportId);
                            refresher.setRefreshing(false);
                        } else {
                            refresher.setRefreshing(false);
                            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );*/
        return rootView;
    }


    private class BaseTripData extends AsyncTask<String, String, JSONObject> {
        String URL = AppStatus.getbaseurl().baseurl() + "get_trips";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String BaseAirportIds = args[1];
            String AirlineIds = args[0];
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("airline_id", AirlineIds));
            params.add(new BasicNameValuePair("base_airport_id", BaseAirportIds));
            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
            return json;
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    int status_id = result.getInt("status_id");
                    String MSG = result.getString("status_msg");
                    if (status_id != 0) {
                        try {
                            tripItemList = new ArrayList<>();
                            JSONArray array = new JSONArray(result.getString("data"));
                            for (int i = 0; i < array.length(); i++) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date testDate = null;
                                try {
                                    testDate = sdf.parse(array.getJSONObject(i).getString("start_date"));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                                String newFormat = formatter.format(testDate);
                                tripItemList.add(new TripItemList(
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
                                Adapter = new AdapterBaseTrip(tripItemList);
                                RvBrowseAll.setAdapter(Adapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        RvBrowseAll.setVisibility(View.GONE);
                        NoTripData.setVisibility(View.VISIBLE);
                        NoTripData.setText(MSG);
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