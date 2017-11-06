package com.trippusher.fragment;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;
import com.trippusher.activity.ActivityEditNotification;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Desktop-KS on 7/28/2017.
 */

public class FragmentNotification extends Fragment {
    private RecyclerView RvNotification;
    private AdapterNotification Adapter;
    private List<NotificationItemList> notificationItemList;
    JSONParser jsonParser = new JSONParser();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String AirlineId, BaseAirportId, userId;
    Fragment fragment;
    FragmentManager fragmentManager;
    SwipeRefreshLayout refresher;
    TextView NoData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        AirlineId = prefs.getString("airlineId", null);
        BaseAirportId = prefs.getString("AirportId", null);
        userId = prefs.getString("userId", null);
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        RvNotification = (RecyclerView) rootView.findViewById(R.id.RvNotification);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        RvNotification.setHasFixedSize(true);
        RvNotification.setLayoutManager(manager);
        NoData = (TextView) rootView.findViewById(R.id.txtNodata);
        refresher = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher);
        if (AppStatus.getInstance(getContext()).isOnline()) {
            NotificationData notificationData = new NotificationData();
            notificationData.execute(AirlineId, BaseAirportId);
        } else {
            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
        }
        refresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (AppStatus.getInstance(getContext()).isOnline()) {
                            NotificationData notificationData = new NotificationData();
                            notificationData.execute(AirlineId, BaseAirportId);
                            refresher.setRefreshing(false);
                        } else {
                            refresher.setRefreshing(false);
                            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        return rootView;
    }

    private class NotificationData extends AsyncTask<String, String, JSONObject> {
        String URL = AppStatus.getbaseurl().baseurl() + "get_notification_list";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String BaseAirportIds = args[1];
            String AirlineIds = args[0];
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("user_id", userId));
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
                            notificationItemList = new ArrayList<>();
                            JSONArray array = new JSONArray(result.getString("data"));
                            for (int i = 0; i < array.length(); i++) {
                                /*Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.valueOf(array.getJSONObject(i).getString("date_added")) * 1000L);
                                date_added = DateFormat.format("MM/dd/yyyy", cal).toString();*/
                                notificationItemList.add(new NotificationItemList(
                                        userId,
                                        array.getJSONObject(i).getString("notification_title"),
                                        array.getJSONObject(i).getString("notification_id"),
                                        array.getJSONObject(i).getString("source_airport_id"),
                                        array.getJSONObject(i).getString("date"),
                                        array.getJSONObject(i).getString("gift"),
                                        array.getJSONObject(i).getString("min_days"),
                                        array.getJSONObject(i).getString("max_days")));
                            }

                            if (Adapter != null) { // it works second time and later
                                Adapter.notifyDataSetChanged();
                            } else { // it works first time
                                Adapter = new AdapterNotification(notificationItemList);
                                RvNotification.setAdapter(Adapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        NoData.setVisibility(View.VISIBLE);
                        RvNotification.setVisibility(View.GONE);
                        NoData.setText(MSG);
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class NotificationItemList {
        public String usersId;
        public String notification_title;
        public String notification_id;
        public String source_airport_id;
        public String date;
        public String gift;
        public String min_days;
        public String max_days;

        public NotificationItemList(String users_id,String notification_title, String notification_id,
                                    String source_airport_id, String date, String gift, String min_days, String max_days) {
            this.usersId = users_id;
            this.notification_title = notification_title;
            this.notification_id = notification_id;
            this.source_airport_id = source_airport_id;
            this.date = date;
            this.gift = gift;
            this.min_days = min_days;
            this.max_days = max_days;
        }
    }

    public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.MyViewHolder> {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        JSONParser jsonParser = new JSONParser();
        private List<FragmentNotification.NotificationItemList> mModelList;

        public AdapterNotification(List<FragmentNotification.NotificationItemList> modelList) {
            mModelList = modelList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_notification, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder ViewHolder, int position) {
            final FragmentNotification.NotificationItemList model = mModelList.get(position);
            ViewHolder.textN.setText(Html.fromHtml("<b>" + "Notification Title:" + "</b> " + model.notification_title));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date testDate = null;
            try {
                testDate = sdf.parse(model.date);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String newFormat = formatter.format(testDate);
            ViewHolder.textD.setText(Html.fromHtml("<b>" + "Date :" + "</b> " + newFormat));
            ViewHolder.layNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment = new FragmentSearchTrip();
                    Bundle bundle = new Bundle();
                    bundle.putString("SpnSearchBase", model.source_airport_id);
                    bundle.putString("Date", model.date);
                    bundle.putString("SpnMinDays", model.min_days);
                    bundle.putString("SpnMaxDays", model.max_days);
                    bundle.putString("SpnSearchGift", model.gift);
                    bundle.putString("NotificationTrip", "NotificationTrip");
                    fragment.setArguments(bundle);
                    fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
            });
            ViewHolder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    editor = prefs.edit();
                    editor.putString("notification_id", model.notification_id);
                    editor.commit();
                    Intent intent = new Intent(getActivity(), ActivityEditNotification.class);
                    startActivity(intent);
                }
            });
            ViewHolder.DeleteAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("Are you sure you want to delete notification ?");
                    builder.setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new AsyncTask<String, String, JSONObject>() {
                                        String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "delete_notification";

                                        @Override
                                        protected void onPreExecute() {
                                            super.onPreExecute();
                                        }

                                        @Override
                                        protected JSONObject doInBackground(String... args) {
                                            ArrayList parametarGetAirline = new ArrayList();
                                            parametarGetAirline.add(new BasicNameValuePair("user_id", model.usersId));
                                            parametarGetAirline.add(new BasicNameValuePair("notification_id", model.notification_id));
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
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    TextView title = new TextView(view.getContext());
                    title.setText("Warning");
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextColor(Color.BLACK);
                    alert.setCustomTitle(title);
                    alert.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mModelList == null ? 0 : mModelList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layNotification;
            public LinearLayout DeleteAll;
            public LinearLayout edit;
            public TextView textN;
            public TextView textD;

            private MyViewHolder(View itemView) {
                super(itemView);
                layNotification = (LinearLayout) itemView.findViewById(R.id.laynotification);
                DeleteAll = (LinearLayout) itemView.findViewById(R.id.Laydelete);
                edit = (LinearLayout) itemView.findViewById(R.id.LayEdit);
                textN = (TextView) itemView.findViewById(R.id.txtnotificationtitle);
                textD = (TextView) itemView.findViewById(R.id.txtdateAdd);
            }
        }
    }
}