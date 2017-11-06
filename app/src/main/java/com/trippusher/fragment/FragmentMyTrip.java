package com.trippusher.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;
import com.trippusher.activity.ActivityMyTripDetail;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Desktop-KS on 7/28/2017.
 */

public class FragmentMyTrip extends Fragment {
    Button BtnFooter;
    private RecyclerView RvBrowseAll;
    private MyTripAdapter Adapter;
    private List<MyTripItemList> myTripItemList;
    JSONParser jsonParser = new JSONParser();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String AirlineId, BaseAirportId, UserId;
    SwipeRefreshLayout refreshers;
    Date date = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


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
        refreshers = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher);
        BtnFooter = (Button) rootView.findViewById(R.id.Btnfooter);
        BtnFooter.setVisibility(View.GONE);
        if (AppStatus.getInstance(getContext()).isOnline()) {
            MyTripData myTripData = new MyTripData();
            myTripData.execute(AirlineId, BaseAirportId);
        } else {
            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
        }
        refreshers.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //refresher.setRefreshing(true);
                        if (AppStatus.getInstance(getContext()).isOnline()) {
                            MyTripData myTripData = new MyTripData();
                            myTripData.execute(AirlineId, BaseAirportId);
                            refreshers.setRefreshing(false);
                        } else {
                            refreshers.setRefreshing(false);
                            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        return rootView;
    }

    private class MyTripData extends AsyncTask<String, String, JSONObject> {
        String URL = AppStatus.getbaseurl().baseurl() + "get_my_trips";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String BaseAirportIds = args[1];
            String AirlineIds = args[0];
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("user_id", UserId));
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
                            int mType = 0;
                            myTripItemList = new ArrayList<>();
                            JSONArray array = new JSONArray(result.getString("data"));
                            Calendar c = Calendar.getInstance();
                            Date currentDate = new Date();
                            c.setTime(currentDate);
                            String CurrentDate = sdf.format(currentDate);
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    date = sdf.parse(array.getJSONObject(i).getString("start_date"));
                                    Date date1 = sdf.parse(CurrentDate);
                                    if (date.before(date1)) {
                                        mType = 2;
                                    } else {
                                        mType = 1;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                myTripItemList.add(new MyTripItemList(
                                        mType,
                                        array.getJSONObject(i).getString("base_airport"),
                                        array.getJSONObject(i).getString("airline_title"),
                                        array.getJSONObject(i).getString("image"),
                                        dateFormat.format(date),
                                        array.getJSONObject(i).getString("flight_time_hrs") + "hrs" + " " + array.getJSONObject(i).getString("flight_time_mins") + " " + "mins",
                                        array.getJSONObject(i).getString("gift"),
                                        array.getJSONObject(i).getString("post_trip_id")));
                            }
                            if (Adapter != null) { // it works second time and later
                                Adapter.notifyDataSetChanged();
                            } else { // it works first time
                                Adapter = new MyTripAdapter(getContext(), myTripItemList);
                                RvBrowseAll.setAdapter(Adapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyTripItemList {
        private int mType;
        public String base_airport;
        public String airline_title;
        public String image;
        public String start_date;
        public String hours;
        public String gift;
        public String post_trip_id;

        public MyTripItemList(int mtype, String base_airport, String airline_title, String image,
                              String start_date, String hours, String gift, String post_trip_id) {
            this.mType = mtype;
            this.base_airport = base_airport;
            this.airline_title = airline_title;
            this.image = image;
            this.start_date = start_date;
            this.hours = hours;
            this.gift = gift;
            this.post_trip_id = post_trip_id;
        }
    }

    public class MyTripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<MyTripItemList> list;
        Context context;

        public MyTripAdapter(Context context, List<MyTripItemList> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).mType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View view = LayoutInflater.from(context).inflate(R.layout.items_triplisting, parent, false);
                return new Valid(view);
            }
            if (viewType == 2) {
                View view = LayoutInflater.from(context).inflate(R.layout.items_triplisting, parent, false);
                return new Expired(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            final MyTripItemList model = list.get(position);
            if (holder instanceof Valid) {
                ((Valid) holder).Image.setImageResource(R.drawable.airlineimage);
                ((Valid) holder).base_airport.setText(model.base_airport);
                ((Valid) holder).airline_title.setText(model.airline_title);
                ((Valid) holder).date.setText(model.start_date);
                ((Valid) holder).hours.setText(model.hours);
                ((Valid) holder).gift.setText(model.gift);
                ((Valid) holder).LayRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ActivityMyTripDetail.class);
                        /*prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                        editor = prefs.edit();
                        editor.putString("MyPostTripId", String.valueOf(model.post_trip_id));
                        editor.commit();*/
                        intent.putExtra("MyPostTripId",String.valueOf(model.post_trip_id));
                        view.getContext().startActivity(intent);
                    }
                });
            } else if (holder instanceof Expired) {
                ((Expired) holder).txtExpired.setText(Html.fromHtml("<b><font color='red'>" + "Expired" + "</font></b> "));
                ((Expired) holder).Image.setImageResource(R.drawable.airlinegreyimage);
                ((Expired) holder).base_airport.setText(model.base_airport);
                ((Expired) holder).airline_title.setText(model.airline_title);
                ((Expired) holder).date.setText(model.start_date);
                ((Expired) holder).hours.setText(model.hours);
                ((Expired) holder).gift.setText(model.gift);
                ((Expired) holder).LayRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ActivityMyTripDetail.class);
                        /*prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                        editor = prefs.edit();
                        editor.putString("MyPostTripId", String.valueOf(model.post_trip_id));
                        editor.commit();*/
                        intent.putExtra("MyPostTripId",String.valueOf(model.post_trip_id));
                        view.getContext().startActivity(intent);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Valid extends RecyclerView.ViewHolder {
            private View view;
            public LinearLayout LayRow;
            public LinearLayout LayExpired;
            public ImageView Image;
            public TextView base_airport;
            public TextView airline_title;
            public TextView date;
            public TextView hours;
            public TextView gift;
            public TextView txtExpired;

            private Valid(View itemView) {
                super(itemView);
                view = itemView;
                base_airport = (TextView) itemView.findViewById(R.id.txtbaseairport);
                LayRow = (LinearLayout) itemView.findViewById(R.id.llrow);
                LayExpired = (LinearLayout) itemView.findViewById(R.id.LayExpired);
                LayExpired.setVisibility(View.GONE);
                txtExpired = (TextView) itemView.findViewById(R.id.txtExpired);
                Image = (ImageView) itemView.findViewById(R.id.img);
                base_airport = (TextView) itemView.findViewById(R.id.txtbaseairport);
                airline_title = (TextView) itemView.findViewById(R.id.txtairlinetitle);
                date = (TextView) itemView.findViewById(R.id.txtstartdate);
                hours = (TextView) itemView.findViewById(R.id.txthours);
                gift = (TextView) itemView.findViewById(R.id.txtgift);
            }
        }

        class Expired extends RecyclerView.ViewHolder {
            private View view;
            public LinearLayout LayRow;
            public LinearLayout LayExpired;
            public ImageView Image;
            public TextView base_airport;
            public TextView airline_title;
            public TextView date;
            public TextView hours;
            public TextView gift;
            public TextView txtExpired;

            public Expired(View itemView) {
                super(itemView);
                view = itemView;
                base_airport = (TextView) itemView.findViewById(R.id.txtbaseairport);
                LayRow = (LinearLayout) itemView.findViewById(R.id.llrow);
                LayExpired = (LinearLayout) itemView.findViewById(R.id.LayExpired);
                LayExpired.setVisibility(View.VISIBLE);
                txtExpired = (TextView) itemView.findViewById(R.id.txtExpired);
                Image = (ImageView) itemView.findViewById(R.id.img);
                base_airport = (TextView) itemView.findViewById(R.id.txtbaseairport);
                airline_title = (TextView) itemView.findViewById(R.id.txtairlinetitle);
                date = (TextView) itemView.findViewById(R.id.txtstartdate);
                hours = (TextView) itemView.findViewById(R.id.txthours);
                gift = (TextView) itemView.findViewById(R.id.txtgift);
            }
        }

    }
}