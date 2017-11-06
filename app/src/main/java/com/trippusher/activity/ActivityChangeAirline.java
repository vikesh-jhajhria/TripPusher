package com.trippusher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
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
import com.trippusher.classes.AirlineList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Desktop-KS on 8/12/2017.
 */

public class ActivityChangeAirline extends Activity {
    ImageView Back;
    String result, userId, resultBase, AirlineTitle,
            AirlineId, AirportCode, AirportId, Password, airlineResult;
    ArrayAdapter Adapter;
    Button btnChangeAirline;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TextView spiAir, Pass;
    ListView AirlineListView;
    AlertDialog DiaAirline;
    private List<AirlineList> AirlineList;
    JSONParser jsonParser = new JSONParser();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_airline);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        spiAir = (TextView) findViewById(R.id.spnChangeAir);
        Back = (ImageView) findViewById(R.id.imgBack);
        Pass = (TextView) findViewById(R.id.txtcheckPass);
        btnChangeAirline = (Button) findViewById(R.id.btnChangeAirline);
        userId = prefs.getString("userId", null);
        resultBase = prefs.getString("resultBase", null);
        AirportId = prefs.getString("AirportId", null);
        AirportCode = prefs.getString("AirportCode", null);
        AirlineTitle = prefs.getString("airlineTitle", null);
        AirlineId = prefs.getString("airlineId", null);
        Password = prefs.getString("password", null);
        spiAir.setText(AirlineTitle);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        spiAir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AppStatus.getInstance(ActivityChangeAirline.this).isOnline()) {
                    airlineResult = prefs.getString("airlineResult", null);
                    if (airlineResult != null) {
                        try {
                            DiaAirline = new AlertDialog.Builder(ActivityChangeAirline.this).create();
                            DiaAirline.setTitle("Select Airline");
                            DiaAirline.setCancelable(true);
                            AirlineListView = new ListView(ActivityChangeAirline.this);
                            if (Adapter == null) {
                                AirlineList = new ArrayList<>();
                                JSONArray array = new JSONArray(airlineResult);
                                for (int i = 0; i < array.length(); i++) {
                                    AirlineList.add(new AirlineList(
                                            array.getJSONObject(i).getString("pk_airline_id"),
                                            array.getJSONObject(i).getString("airline_title")));
                                }
                                Adapter = new ArrayAdapter(ActivityChangeAirline.this, android.R.layout.simple_list_item_1, AirlineList);
                            }
                            AirlineListView.setAdapter(Adapter);
                            DiaAirline.setView(AirlineListView);
                            DiaAirline.show();

                            AirlineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DiaAirline.cancel();
                                    String s = AirlineListView.getItemAtPosition(position).toString();
                                    spiAir.setText(s);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        getAirline();
                    }
                } else {
                    Toast.makeText(ActivityChangeAirline.this, "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                }
            }

        });
        btnChangeAirline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pass.getText().toString().matches("")) {
                    Toast.makeText(ActivityChangeAirline.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                } else {
                    if (Pass.getText().toString().matches(Password)) {
                        if (!spiAir.getText().equals(AirlineTitle)) {
                            try {
                                JSONArray array1 = new JSONArray(airlineResult);
                                for (int i = 0; i < array1.length(); i++) {
                                    if (spiAir.getText().equals(array1.getJSONObject(i).getString("airline_title"))) {
                                        AirlineId = array1.getJSONObject(i).getString("pk_airline_id");
                                        editor.putString("airlineTitle", array1.getJSONObject(i).getString("airline_title"));
                                        editor.putString("airlineId", array1.getJSONObject(i).getString("pk_airline_id"));
                                        editor.commit();
                                        updateAirline();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            finish();
                        }
                    } else {
                        Toast.makeText(ActivityChangeAirline.this, "Please Check Your Password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void getAirline() {
        new AsyncTask<String, String, JSONObject>() {
            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "get_airlines";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(String... args) {
                ArrayList parametarGetAirline = new ArrayList();
                parametarGetAirline.add(new BasicNameValuePair("status_id", "1"));
                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                return json;
            }

            protected void onPostExecute(JSONObject result) {
                try {
                    if (result != null) {
                        int status_id = result.getInt("status_id");
                        String MSG = result.getString("status_msg");
                        if (status_id != 0) {

                            editor.putString("airlineResult", result.getString("data"));
                            editor.commit();
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
    }

    private void getAirport() {
        try {
            JSONArray array1 = new JSONArray(airlineResult);
            for (int i = 0; i < array1.length(); i++) {
                if (spiAir.getText().equals(array1.getJSONObject(i).getString("airline_title"))) {
                    AirlineId = array1.getJSONObject(i).getString("pk_airline_id");
                    new AsyncTask<String, String, JSONObject>() {
                        String URLs = AppStatus.getbaseurl().baseurl() + "get_airports";

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            ArrayList parametarGetAirport = new ArrayList();
                            parametarGetAirport.add(new BasicNameValuePair("status_id", "1"));
                            parametarGetAirport.add(new BasicNameValuePair("airline_id", AirlineId));
                            JSONObject json = jsonParser.makeHttpRequest(URLs, "POST", parametarGetAirport);
                            return json;
                        }

                        protected void onPostExecute(JSONObject result) {
                            try {
                                if (result != null) {
                                    int status_id = result.getInt("status_id");
                                    String MSG = result.getString("status_msg");
                                    if (status_id != 0) {
                                        editor.putString("resultBase", result.getString("data"));
                                        editor.commit();
                                        JSONArray array = new JSONArray(result.getString("data"));
                                        int flag = 0;
                                        for (int i = 0; i < array.length(); i++) {
                                            if (AirportCode.matches(array.getJSONObject(i).getString("airport_code"))) {
                                                flag = 1;
                                                editor.putString("AirportId", array.getJSONObject(i).getString("pk_airport_id"));
                                                editor.commit();
                                                AirportId = array.getJSONObject(i).getString("pk_airport_id");
                                            }
                                        }
                                        if (flag == 1) {

                                            new AsyncTask<String, String, JSONObject>() {
                                                String URL = AppStatus.getbaseurl().baseurl() + "update_base_airport";

                                                @Override
                                                protected void onPreExecute() {
                                                    super.onPreExecute();
                                                }

                                                @Override
                                                protected JSONObject doInBackground(String... args) {
                                                    ArrayList parametarGetAirline = new ArrayList();
                                                    parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                                                    parametarGetAirline.add(new BasicNameValuePair("base_airport_id", AirportId));
                                                    JSONObject json = jsonParser.makeHttpRequest(URL, "POST", parametarGetAirline);
                                                    return json;
                                                }

                                                protected void onPostExecute(JSONObject result) {
                                                    try {
                                                        if (result != null) {
                                                            int status_id = result.getInt("status_id");
                                                            String MSG = result.getString("status_msg");
                                                            if (status_id != 0) {
                                                                Toast.makeText(getApplicationContext(), "Airline update successfully", Toast.LENGTH_LONG).show();
                                                                //editor.putString("ChangeProfile", "Airline update successfully");
                                                                //editor.commit();
                                                                Intent activity2 = new Intent(ActivityChangeAirline.this, ActivityMain.class);
                                                                activity2.putExtra("ChangeProfile", "ChangeProfile");
                                                                finish();
                                                                startActivity(activity2);
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
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityChangeAirline.this);
                                            builder.setMessage("you need to change base");
                                            builder.setCancelable(false)
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            editor.putString("AirportId", "0");
                                                            editor.putString("AirportCode", "Select Base");
                                                            editor.commit();
                                                            Intent refresh = new Intent(ActivityChangeAirline.this, ActivityChangeBase.class);
                                                            startActivity(refresh);
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            TextView title = new TextView(ActivityChangeAirline.this);
                                            title.setText("Warning");
                                            title.setPadding(10, 10, 10, 10);
                                            title.setGravity(Gravity.CENTER);
                                            title.setTextColor(Color.BLACK);
                                            alert.setCustomTitle(title);
                                            alert.show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateAirline() {
        new AsyncTask<String, String, JSONObject>() {
            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "change_airline";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(String... args) {
                ArrayList parametarGetAirline = new ArrayList();
                parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                parametarGetAirline.add(new BasicNameValuePair("airline_id", AirlineId));
                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                return json;
            }

            protected void onPostExecute(JSONObject result) {
                try {
                    if (result != null) {
                        int status_id = result.getInt("status_id");
                        String MSG = result.getString("status_msg");
                        if (status_id != 0) {
                            getAirport();
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
    }
}