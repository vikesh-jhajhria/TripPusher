package com.trippusher.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Desktop-KS on 8/10/2017.
 */

public class ActivityMyTripDetail extends Activity {
    String userId, postTripId, receiverId;
    TextView Base, TripId, NoDays, AirlineTitle, Gift, Hours, StartDate, Message, btnDelete, btnEdit;
    ImageView Back;
    LinearLayout layEdit, layDelete, layMyTrip, layMessageCall;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    JSONParser jsonParser = new JSONParser();
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tripdetail);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        userId = prefs.getString("userId", null);
        Bundle bundle = getIntent().getExtras();
        postTripId = bundle.getString("MyPostTripId");
        Base = (TextView) findViewById(R.id.txtDetailBase);
        TripId = (TextView) findViewById(R.id.txtDetailTripId);
        NoDays = (TextView) findViewById(R.id.txtDetailNoDays);
        AirlineTitle = (TextView) findViewById(R.id.txtDetailairlinetitle);
        Gift = (TextView) findViewById(R.id.txtDetailGift);
        Hours = (TextView) findViewById(R.id.txttrphrs);
        StartDate = (TextView) findViewById(R.id.txtDetailStartDate);
        Message = (TextView) findViewById(R.id.txtDetailMessage);
        layEdit = (LinearLayout) findViewById(R.id.layMessage);
        layMessageCall = (LinearLayout) findViewById(R.id.layMessageCall);
        layMyTrip = (LinearLayout) findViewById(R.id.layMyTrip);
        layDelete = (LinearLayout) findViewById(R.id.layCall);
        Back = (ImageView) findViewById(R.id.imgBack);
        btnDelete = (TextView) findViewById(R.id.btnRight);
        btnEdit = (TextView) findViewById(R.id.btnLeft);
        btnDelete.setText("Delete");
        btnEdit.setText("Edit");
        layDelete.setVisibility(View.VISIBLE);
        layEdit.setVisibility(View.VISIBLE);

        new AsyncTask<String, String, JSONObject>() {
            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "get_trip_details";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(String... args) {
                ArrayList parametarGetAirline = new ArrayList();
                parametarGetAirline.add(new BasicNameValuePair("post_trip_id", postTripId));
                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                return json;
            }

            protected void onPostExecute(JSONObject result) {
                try {
                    if (result != null) {
                        int status_id = result.getInt("status_id");
                        String MSG = result.getString("status_msg");
                        if (status_id != 0) {
                            JSONArray array = new JSONArray(result.getString("data"));
                            receiverId = array.getJSONObject(0).getString("user_id");
                            Base.setText(array.getJSONObject(0).getString("base_airport"));
                            TripId.setText(array.getJSONObject(0).getString("trip_id"));
                            NoDays.setText(array.getJSONObject(0).getString("no_of_days"));
                            Gift.setText(array.getJSONObject(0).getString("gift"));
                            Hours.setText(array.getJSONObject(0).getString("flight_time_hrs") + "hrs" + " " + array.getJSONObject(0).getString("flight_time_mins") + " " + "mins");
                            Message.setText(array.getJSONObject(0).getString("message"));
                            try {
                                StartDate.setText(dateFormat.format(sdf.parse(array.getJSONObject(0).getString("start_date"))));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            String call = array.getJSONObject(0).getString("allow_call");
                            AirlineTitle.setText(array.getJSONObject(0).getString("airline_title"));
                        } else {
                            Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ActivityEditMyTrip.class);
                intent.putExtra("MyPostTripId",postTripId);
                startActivity(intent);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMyTripDetail.this);
                builder.setMessage("Are you sure you want to delete this trip ?");
                builder.setCancelable(false)
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new AsyncTask<String, String, JSONObject>() {
                                    String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "delete_my_trip";

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        ArrayList parametarGetAirline = new ArrayList();
                                        parametarGetAirline.add(new BasicNameValuePair("post_trip_id", postTripId));
                                        parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                                        JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                        return json;
                                    }

                                    protected void onPostExecute(JSONObject result) {
                                        try {
                                            if (result != null) {
                                                int status_id = result.getInt("status_id");
                                                String MSG = result.getString("status_msg");
                                                if (status_id != 0) {
                                                    Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(getApplication(), ActivityMain.class);
                                                    intent.putExtra("EditMyTrips", "EditMyTrips");
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
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
                TextView title = new TextView(ActivityMyTripDetail.this);
                title.setText("Warning");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.show();
            }
        });
    }
}