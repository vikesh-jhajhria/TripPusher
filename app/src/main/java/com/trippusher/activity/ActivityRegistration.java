package com.trippusher.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.Model;
import com.trippusher.R;
import com.trippusher.classes.AirlineList;
import com.trippusher.classes.AirportList;
import com.trippusher.util.IabHelper;
import com.trippusher.util.IabResult;
import com.trippusher.util.Inventory;
import com.trippusher.util.Purchase;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Desktop-KS on 7/21/2017.
 */

public class ActivityRegistration extends AppCompatActivity {
    static final String ITEM_SKU = "com.trippusher.monthly5";
    static final String ITEM_SKUs = "com.trippusher.yearly50";
    private static final String TAG = "InAppBilling";
    String FirstName, LastName, userName, userPass, VPassword, userMobile, userEmail;
    TextView txtHeader, spiAir, spiBase, Subscription, txtTandC;
    ImageView Back;
    EditText user_name, user_mobile, user_pass, txtFirstName, txtLastName, user_Email, VerifyPassword;
    ArrayAdapter adapter, BaseAirAdapter;
    Button registration;
    String airlineResult, resultBase, airline_id, airport_id;
    RadioButton radio_5, radio_50;
    AlertDialog DiaAirline, DiaBaseAirport;
    ListView AirlineListView, BaseAirportListView;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    JSONParser jsonParser = new JSONParser();
    int radio_id;
    CheckBox chkTandC;
    IabHelper mHelper;
    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl6C3QCqJ0J8SsjggS3ENyRnBBap4SO6MHyp9BqsjuR6ZJNUZyVwLhUXOu36ZZaZDXuHkOrdSVY0su2s7AZzjNigfxZcAFio40gHy/ev//ZDodKsTOum1WI8inQ6CXDiAbcU3tWogsFYGnrH5qUFVcv7tHmi0mLXWPC2gFNCNg/tUwcuQg8h6I02spPk+nIhg7KyGVfDKmZ11mdgskEZ5OQZi6OVGAzVUanOplHwYAcUlonZiY+uEckFRfNuEJfZKC2liqZ7gWYAcCj3hyVfdOcXzI2Gw4IIUCIjRs02l12T8zD/BXqEyGhhkEsxRzttzdz7Ny3qeYkFmV9+dmJjUiwIDAQAB";
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                //return;
                Toast.makeText(getApplicationContext(), "User cancelled", Toast.LENGTH_LONG).show();
                //Log.d("cancel result", result.toString());
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                //consumeItem();
                //buyButton.setEnabled(false);
                Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {

                    if (result.isSuccess()) {
                        //clickButton.setEnabled(true);
                        new AsyncTask<String, String, JSONObject>() {
                            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "create_user/";

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected JSONObject doInBackground(String... args) {
                                ArrayList parametarGetAirport = new ArrayList();
                                parametarGetAirport.add(new BasicNameValuePair("airline_id", airline_id));
                                parametarGetAirport.add(new BasicNameValuePair("base_airport_id", airport_id));
                                parametarGetAirport.add(new BasicNameValuePair("first_name", FirstName));
                                parametarGetAirport.add(new BasicNameValuePair("last_name", LastName));
                                parametarGetAirport.add(new BasicNameValuePair("user_name", userName));
                                String mobil = userMobile.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                                parametarGetAirport.add(new BasicNameValuePair("phone_no", mobil));
                                parametarGetAirport.add(new BasicNameValuePair("password", userPass));
                                parametarGetAirport.add(new BasicNameValuePair("email", userEmail));
                                parametarGetAirport.add(new BasicNameValuePair("subscription_plan", "" + radio_id));
                                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirport);
                                return json;
                            }

                            protected void onPostExecute(JSONObject result) {
                                try {
                                    if (result != null) {
                                        int status_id = result.getInt("status_id");
                                        String MSG = result.getString("status_msg");
                                        if (status_id != 0) {
                                            Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(getApplication(), ActivityLogin.class);
                                            startActivity(intent);
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
                        Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            };
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };
    private List<AirlineList> AirlineList;
    private List<AirportList> AirportList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Back = (ImageView) findViewById(R.id.imgBack);
        txtHeader = (TextView) findViewById(R.id.txtHeader);
        txtHeader.setText(Html.fromHtml("<i>Sign Up For Your Account Today"));
        registration = (Button) findViewById(R.id.btnRsignup);
        spiAir = (TextView) findViewById(R.id.spiAir);
        spiBase = (TextView) findViewById(R.id.spiBase);
        Subscription = (TextView) findViewById(R.id.Subscription);
        Subscription.setText(Html.fromHtml("<i>While you won't be charged for your free 60-day trial, you will be automatically upgraded to the paid membership plan of your choice at the end of the trial period. You can cancel this at any time</i>"));
        txtFirstName = (EditText) findViewById(R.id.txtFirstName);
        txtLastName = (EditText) findViewById(R.id.txtLastName);
        user_name = (EditText) findViewById(R.id.txtUsername);
        user_mobile = (EditText) findViewById(R.id.txtMobile);
        user_Email = (EditText) findViewById(R.id.txtEmail);
        user_mobile.addTextChangedListener(new Model.PhoneNumberFormattingTextWatcher(user_mobile));
        user_pass = (EditText) findViewById(R.id.txtPassword);
        VerifyPassword = (EditText) findViewById(R.id.txtVerifyPassword);
        radio_5 = (RadioButton) findViewById(R.id.radio5);
        radio_50 = (RadioButton) findViewById(R.id.radio50);
        chkTandC = (CheckBox) findViewById(R.id.chkTandC);
        txtTandC = (TextView) findViewById(R.id.txtTandC);
        if (AppStatus.getInstance(ActivityRegistration.this).isOnline()) {
            getAirline();
        }


        spiAir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AppStatus.getInstance(ActivityRegistration.this).isOnline()) {
                    airlineResult = prefs.getString("airlineResult", null);
                    if (airlineResult != null) {
                        try {
                            DiaAirline = new AlertDialog.Builder(ActivityRegistration.this).create();
                            DiaAirline.setTitle("Select Airline");
                            DiaAirline.setCancelable(true);
                            AirlineListView = new ListView(ActivityRegistration.this);
                            if (adapter == null) {
                                AirlineList = new ArrayList<>();
                                JSONArray array = new JSONArray(airlineResult);
                                for (int i = 0; i < array.length(); i++) {
                                    AirlineList.add(new AirlineList(
                                            array.getJSONObject(i).getString("pk_airline_id"),
                                            array.getJSONObject(i).getString("airline_title")));
                                }
                                adapter = new ArrayAdapter(ActivityRegistration.this, android.R.layout.simple_list_item_1, AirlineList);
                            }
                            AirlineListView.setAdapter(adapter);
                            DiaAirline.setView(AirlineListView);
                            DiaAirline.show();
                            AirlineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DiaAirline.cancel();
                                    String s = AirlineListView.getItemAtPosition(position).toString();
                                    spiAir.setText(s);
                                    spiBase.setText("Select Base");
                                    editor.putString("resultBase", null);
                                    editor.commit();
                                    getAirport();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        getAirline();
                    }
                } else {
                    Toast.makeText(ActivityRegistration.this,
                            "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                }
            }

        });
        spiBase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (spiAir.getText().equals("Select Airline")) {
                    Toast.makeText(getApplicationContext(), "Please Select Airline", Toast.LENGTH_LONG).show();
                } else {
                    if (AppStatus.getInstance(ActivityRegistration.this).isOnline()) {
                        resultBase = prefs.getString("resultBase", null);
                        if (resultBase != null) {
                            try {
                                DiaBaseAirport = new AlertDialog.Builder(ActivityRegistration.this).create();
                                DiaBaseAirport.setTitle("Select Airport");
                                BaseAirportListView = new ListView(ActivityRegistration.this);
                                AirportList = new ArrayList<>();
                                JSONArray array2 = new JSONArray(resultBase);
                                for (int i = 0; i < array2.length(); i++) {
                                    AirportList.add(new AirportList(
                                            array2.getJSONObject(i).getString("pk_airport_id"),
                                            array2.getJSONObject(i).getString("airport_code")));
                                }
                                BaseAirAdapter = new ArrayAdapter(ActivityRegistration.this, android.R.layout.simple_list_item_1, AirportList);
                                BaseAirportListView.setAdapter(BaseAirAdapter);
                                DiaBaseAirport.setView(BaseAirportListView);
                                DiaBaseAirport.show();
                                BaseAirportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        DiaBaseAirport.cancel();
                                        String s = BaseAirportListView.getItemAtPosition(position).toString();
                                        spiBase.setText(s);
                                        try {
                                            JSONArray array1 = new JSONArray(resultBase);
                                            for (int i = 0; i < array1.length(); i++) {
                                                if (spiBase.getText().equals(array1.getJSONObject(i).getString("airport_code"))) {
                                                    airport_id = array1.getJSONObject(i).getString("pk_airport_id");
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            getAirport();
                        }
                    } else {
                        Toast.makeText(ActivityRegistration.this,
                                "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                }
            }
        });


        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ActivityLogin.class);
                startActivity(intent);
                finish();
            }
        });
        txtTandC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ActivityTandC.class);
                startActivity(intent);
                finish();
            }
        });
        registration.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FirstName = txtFirstName.getText().toString();
                LastName = txtLastName.getText().toString();
                userName = user_name.getText().toString();
                userPass = user_pass.getText().toString();
                VPassword = VerifyPassword.getText().toString();
                userMobile = user_mobile.getText().toString();
                userEmail = user_Email.getText().toString();
                if (spiAir.getText().equals("Select Airline")) {
                    Toast.makeText(getApplicationContext(), "Please Select Airline", Toast.LENGTH_LONG).show();
                } else if (spiBase.getText().equals("Select Base")) {
                    Toast.makeText(getApplicationContext(), "Please Select Base", Toast.LENGTH_LONG).show();
                } else if (txtFirstName.getText().toString().matches("")) {
                    Toast.makeText(getApplicationContext(), txtFirstName.getText() + "Please Enter First Name", Toast.LENGTH_LONG).show();
                } else if (LastName.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Last Name", Toast.LENGTH_LONG).show();
                } else if (userName.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Name", Toast.LENGTH_LONG).show();
                } else if (userPass.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Password", Toast.LENGTH_LONG).show();
                } else if (VPassword.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Verify Password", Toast.LENGTH_LONG).show();
                } else if (userMobile.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Mobile", Toast.LENGTH_LONG).show();
                } else if (userEmail.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Email", Toast.LENGTH_LONG).show();
                } else {
                    if (!userPass.equals(VPassword)) {
                        Toast.makeText(getApplicationContext(), "Verify password not matching with new passsword", Toast.LENGTH_LONG).show();
                    } else if (!userEmail.trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                        Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                    } else {
                        if (radio_5.isChecked()) {
                            radio_id = 1;
                            if (chkTandC.isChecked()) {
                                mHelper.launchPurchaseFlow(ActivityRegistration.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "com.trippusher.monthly5");
                            } else {
                                Toast.makeText(getApplicationContext(), "Please check your terms and conditions", Toast.LENGTH_LONG).show();
                            }

                        } else if (radio_50.isChecked()) {
                            radio_id = 2;
                            if (chkTandC.isChecked()) {
                                mHelper.launchPurchaseFlow(ActivityRegistration.this, ITEM_SKUs, 10001, mPurchaseFinishedListener, "com.trippusher.yearly50");
                            } else {
                                Toast.makeText(getApplicationContext(), "Please check your terms and conditions", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "choose your subscription", Toast.LENGTH_LONG).show();
                        }
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
                    airline_id = array1.getJSONObject(i).getString("pk_airline_id");
                    new AsyncTask<String, String, JSONObject>() {
                        String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "get_airports";

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            ArrayList parametarGetAirport = new ArrayList();
                            parametarGetAirport.add(new BasicNameValuePair("status_id", "1"));
                            parametarGetAirport.add(new BasicNameValuePair("airline_id", airline_id));
                            JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirport);
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

    /*public void buyClick(View view)
    {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001, mPurchaseFinishedListener, "mypurchasetoken");
    }*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(getApplicationContext(),requestCode +resultCode+data.toString(),Toast.LENGTH_LONG).show();
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void consumeItem()

    {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
