package com.trippusher.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Desktop-KS on 8/9/2017.
 */

public class ActivityTripDetail extends Activity {
    String result, userId, postTripId, receiverId, receiverEmailId, fcm_id, ReceiverFcmId, ChatLocation, trip_id;
    TextView Base, TripId, NoDays, AirlineTitle, Gift, Hours, StartDate, Message, btnCall, btnMessage, btnMyTrip;
    ImageView Back;
    LinearLayout layMessage, layCall, layMyTrip, layMessageCall;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    JSONParser jsonParser = new JSONParser();
    private DatabaseReference conversationsRef, myRef2;
    private FirebaseDatabase database;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tripdetail);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        userId = prefs.getString("userId", null);
        Bundle bundle = getIntent().getExtras();
        postTripId = bundle.getString("postTripId");
        fcm_id = prefs.getString("fcm_id", null);
        Base = (TextView) findViewById(R.id.txtDetailBase);
        TripId = (TextView) findViewById(R.id.txtDetailTripId);
        NoDays = (TextView) findViewById(R.id.txtDetailNoDays);
        AirlineTitle = (TextView) findViewById(R.id.txtDetailairlinetitle);
        Gift = (TextView) findViewById(R.id.txtDetailGift);
        Hours = (TextView) findViewById(R.id.txttrphrs);
        StartDate = (TextView) findViewById(R.id.txtDetailStartDate);
        Message = (TextView) findViewById(R.id.txtDetailMessage);
        layMessage = (LinearLayout) findViewById(R.id.layMessage);
        layMessageCall = (LinearLayout) findViewById(R.id.layMessageCall);
        layMyTrip = (LinearLayout) findViewById(R.id.layMyTrip);
        layCall = (LinearLayout) findViewById(R.id.layCall);
        Back = (ImageView) findViewById(R.id.imgBack);
        btnCall = (TextView) findViewById(R.id.btnRight);
        btnMessage = (TextView) findViewById(R.id.btnLeft);
        btnMyTrip = (TextView) findViewById(R.id.btnCenter);
        btnCall.setText("Call");
        btnMessage.setText("Message");
        btnMyTrip.setText("You Created This Trip");
        ReceiverFcmId = null;
        database = FirebaseDatabase.getInstance();
        conversationsRef = database.getReference("users");
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
                            trip_id = array.getJSONObject(0).getString("trip_id");
                            Base.setText(array.getJSONObject(0).getString("base_airport"));
                            TripId.setText(trip_id);
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
                            editor.putString("call", array.getJSONObject(0).getString("phone_no"));
                            receiverEmailId = array.getJSONObject(0).getString("user_email");
                            editor.commit();
                            if (!receiverId.equals(userId)) {
                                layMessageCall.setVisibility(View.VISIBLE);
                                if (call.equals("0")) {
                                    layMessage.setGravity(Gravity.CENTER);
                                    layMessage.setVisibility(View.VISIBLE);
                                } else {
                                    layCall.setVisibility(View.VISIBLE);
                                    layMessage.setVisibility(View.VISIBLE);
                                }
                            } else {
                                layMyTrip.setVisibility(View.VISIBLE);
                            }
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
        btnMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                conversationsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            if (item.child("credentials").child("email").getValue() != null && receiverEmailId.matches(item.child("credentials").child("email").getValue().toString())) {
                                ReceiverFcmId = item.getKey();
                            }
                        }
                        if (ReceiverFcmId != null) {
                            next();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });
            }
        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String call = prefs.getString("call", null);
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + call));
                startActivity(intent);
            }
        });
    }
    private void next() {
        conversationsRef.child(fcm_id).child("conversations").child(ReceiverFcmId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {
                if (dataSnapshot1 != null) {
                    Map map1 = (Map) dataSnapshot1.getValue();
                    if (map1 != null) {
                        ChatLocation = (String) map1.get("location");
                        new AsyncTask<String, String, JSONObject>() {
                            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "trip_message";

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected JSONObject doInBackground(String... args) {
                                ArrayList parametarGetAirline = new ArrayList();
                                parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                                parametarGetAirline.add(new BasicNameValuePair("trip_id", postTripId));
                                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                return json;
                            }

                            protected void onPostExecute(JSONObject result) {
                                try {
                                    if (result != null) {
                                        int status_id = result.getInt("status_id");
                                        Intent intent = new Intent(ActivityTripDetail.this, ActivityChat.class);
                                        Bitmap b = null;
                                        intent.putExtra("ChatLocation", ChatLocation);
                                        intent.putExtra("ReceiverFcmId", ReceiverFcmId);
                                        intent.putExtra("BitmapImage", b);
                                        if (status_id == 1) {
                                            myRef2 = database.getReference("conversations").child(ChatLocation);
                                            Boolean boolean1 = Boolean.valueOf("false");
                                            int time = (int) (System.currentTimeMillis() / 1000);
                                            /*ActivityChat.ChatList data = new ActivityChat.ChatList();
                                            data.setContent("This user has messaged you about Trip ID:" + trip_id);
                                            data.setFromID(fcm_id);
                                            data.setIsRead(boolean1);
                                            data.setTimestamp(time);
                                            data.setToID(ReceiverFcmId);
                                            data.setType("location");
                                            myRef2.child(myRef2.push().getKey()).setValue(data);*/

                                            ActivityChat.ChatData data = new ActivityChat.ChatData();
                                            data.setcontent("This user has messaged you about Trip ID:" + trip_id);
                                            data.setfromID(fcm_id);
                                            data.setisRead(boolean1);
                                            data.settimestamp(time);
                                            data.settoID(ReceiverFcmId);
                                            data.settype("location");
                                            myRef2.child(myRef2.push().getKey()).setValue(data);
                                            startActivity(intent);
                                            finish();
                                        } else if (status_id == 2) {
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Unable to retrieve any data from server try Again", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.execute();

                    } else {
                        String Key = conversationsRef.push().getKey();
                        conversationsRef.child(fcm_id).child("conversations").child(ReceiverFcmId).child("location").setValue(Key);
                        conversationsRef.child(ReceiverFcmId).child("conversations").child(fcm_id).child("location").setValue(Key);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }
}
