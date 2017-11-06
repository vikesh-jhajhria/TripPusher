package com.trippusher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;
import com.trippusher.classes.AirportList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Desktop-KS on 8/12/2017.
 */

public class ActivityChangeBase extends Activity {
    ImageView Back;
    String result, userid, resultBase, AirportCode, AirportId;
    ArrayAdapter BaseAdapter;
    Button btnChangeBase;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TextView SpnBase;
    ListView BaseAirportListView;
    AlertDialog DiaBaseAirport;
    private List<AirportList> AirportList = new ArrayList<>();
    JSONParser jsonParser = new JSONParser();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changebase);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        SpnBase = (TextView) findViewById(R.id.spnChangeBase);
        Back = (ImageView) findViewById(R.id.imgBack);
        btnChangeBase = (Button) findViewById(R.id.btnChangeBase);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userid = prefs.getString("userId", null);
        resultBase = prefs.getString("resultBase", null);
        AirportId = prefs.getString("AirportId", null);
        AirportCode = prefs.getString("AirportCode", null);
        SpnBase.setText(AirportCode);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnChangeBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!SpnBase.getText().toString().matches(AirportCode)) {
                    AirportCode = SpnBase.getText().toString();
                    new AsyncTask<String, String, JSONObject>() {
                        String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "update_base_airport";

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            ArrayList parametarGetAirline = new ArrayList();
                            parametarGetAirline.add(new BasicNameValuePair("user_id", userid));
                            parametarGetAirline.add(new BasicNameValuePair("base_airport_id", AirportId));
                            JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                            return json;
                        }

                        protected void onPostExecute(JSONObject result) {
                            try {
                                if (result != null) {
                                    Log.d("result", result.toString());
                                    int status_id = result.getInt("status_id");
                                    String MSG = result.getString("status_msg");
                                    if (status_id != 0) {
                                        Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                                        //editor.putString("ChangeProfile", AirportId);
                                        editor.putString("AirportId", AirportId);
                                        editor.putString("AirportCode", AirportCode);
                                        editor.commit();
                                        Intent activity2 = new Intent(ActivityChangeBase.this, ActivityMain.class);
                                        activity2.putExtra("ChangeProfile", "ChangeProfile");
                                        finish();
                                        startActivity(activity2);
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
                } else {
                    finish();
                }
            }
        });
        SpnBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    DiaBaseAirport = new AlertDialog.Builder(ActivityChangeBase.this).create();
                    DiaBaseAirport.setTitle("Select Airport");
                    BaseAirportListView = new ListView(ActivityChangeBase.this);
                    if (BaseAdapter == null) {
                        JSONArray array2 = new JSONArray(resultBase);
                        for (int i = 0; i < array2.length(); i++) {
                            AirportList.add(new AirportList(
                                    array2.getJSONObject(i).getString("pk_airport_id"),
                                    array2.getJSONObject(i).getString("airport_code")));
                        }
                        BaseAdapter = new ArrayAdapter(ActivityChangeBase.this, android.R.layout.simple_list_item_1, AirportList);
                    }
                    BaseAirportListView.setAdapter(BaseAdapter);
                    DiaBaseAirport.setView(BaseAirportListView);
                    DiaBaseAirport.show();
                    BaseAirportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DiaBaseAirport.cancel();
                            String s = BaseAirportListView.getItemAtPosition(position).toString();
                            SpnBase.setText(s);
                            try {
                                JSONArray array1 = new JSONArray(resultBase);
                                for (int i = 0; i < array1.length(); i++) {
                                    if (SpnBase.getText().equals(array1.getJSONObject(i).getString("airport_code"))) {
                                        AirportId = array1.getJSONObject(i).getString("pk_airport_id");
                                    }
                                }
                            } catch (JSONException e) {
                            }
                        }

                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
