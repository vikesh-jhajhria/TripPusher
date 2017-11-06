package com.trippusher.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Desktop-KS on 8/12/2017.
 */

public class ActivityChangePass extends Activity {
    EditText txtRptPass, txtNewPass;
    ImageView Back;
    Button btnChangePassword;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String userId, old_password, NewPass;
    JSONParser jsonParser = new JSONParser();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        Back = (ImageView) findViewById(R.id.imgBack);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        txtRptPass = (EditText) findViewById(R.id.txtRptPass);
        txtNewPass = (EditText) findViewById(R.id.txtNewPass);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userId", null);
        old_password = prefs.getString("old_password", null);
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNewPass.getText().toString().matches("")) {
                    Toast.makeText(ActivityChangePass.this, "Please Enter Your New Password", Toast.LENGTH_SHORT).show();
                } else if (!txtNewPass.getText().toString().matches(txtRptPass.getText().toString())) {
                    Toast.makeText(ActivityChangePass.this, "password did not match", Toast.LENGTH_SHORT).show();
                } else {
                    NewPass = txtNewPass.getText().toString();
                    new AsyncTask<String, String, JSONObject>() {
                        String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "change_password";

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            ArrayList parametarGetAirline = new ArrayList();
                            parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                            parametarGetAirline.add(new BasicNameValuePair("old_password", old_password));
                            parametarGetAirline.add(new BasicNameValuePair("new_password", NewPass));
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
                                        //editor.putString("ChangeProfile", NewPass);
                                        editor.putString("password", NewPass);
                                        editor.commit();
                                        Intent activity2 = new Intent(ActivityChangePass.this, ActivityMain.class);
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
                }
            }
        });
    }
}