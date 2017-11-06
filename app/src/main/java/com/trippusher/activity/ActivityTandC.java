package com.trippusher.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ActivityTandC extends AppCompatActivity {

    ImageView Back;
    WebView view;
    JSONParser jsonParser = new JSONParser();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tand_c);
        view = (WebView) findViewById(R.id.textContent);
        Back = (ImageView) findViewById(R.id.imgBack);
        new AsyncTask<String, String, JSONObject>() {
            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "terms_conditions";
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
                        //byte[] decodeValue = Base64.decode(result.getString("content"), Base64.DEFAULT);
                        //Log.d("TEST", "decodeValue = " + new String(decodeValue));
                        //String tandc=new String(decodeValue);
                        String decodeValue = new String(Base64.decode(result.getString("content"), Base64.DEFAULT));
                        String text;
                        text = "<html><body><p align=\"justify\">";
                        text+= decodeValue;
                        text+= "</p></body></html>";
                        view.loadData(text, "text/html", "utf-8");
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Unable to retrieve any data from server try Again", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ActivityRegistration.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
