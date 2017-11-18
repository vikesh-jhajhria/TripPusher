package com.trippusher.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Desktop-KS on 7/29/2017.
 */

public class ActivityLogin extends AppCompatActivity {
    Button btnLogin;
    TextView txtSignUp, textForgotPass;
    EditText editUserName, editPassword, editForgetPass;
    JSONParser jsonParser = new JSONParser();
    CheckBox RememberMe, ShowMe;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String username, password, rememberUserName, rememberPassword, DeviceToken, forgetPass,user_id,user_name, user_email, fcm_id;
    int i = 0;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private FirebaseDatabase database;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        editUserName = (EditText) findViewById(R.id.txtUser);
        editPassword = (EditText) findViewById(R.id.txtPass);
        RememberMe = (CheckBox) findViewById(R.id.chkRemember);
        ShowMe = (CheckBox) findViewById(R.id.chkShow);
        btnLogin = (Button) findViewById(R.id.btnlogin);
        txtSignUp = (TextView) findViewById(R.id.txtsignup);
        textForgotPass = (TextView) findViewById(R.id.txtForgotPass);
        textForgotPass.setText(Html.fromHtml("<i><font color='#0082F3'>Forgot Password?</font></i>"));
        txtSignUp.setText(Html.fromHtml("<i>Don't have an account?</i>" + "&nbsp;" + " <font color='#0082F3'>" + "<u>Signup</u>" + " </font>"));
        username = prefs.getString("username", null);
        password = prefs.getString("password", null);
        rememberUserName = prefs.getString("Rememberusername", null);
        rememberPassword = prefs.getString("Rememberpassword", null);
        DeviceToken = prefs.getString("refreshedToken", null);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("new_user");
        if (username != null && password != null) {
            editor.putString("drawerstart", "Start");
            editor.commit();
            Intent intent = new Intent(this, ActivityMain.class);
            startActivity(intent);
            finish();
        }
        if (rememberUserName != null && rememberPassword != null) {
            editUserName.setText(rememberUserName);
            editPassword.setText(rememberPassword);
            RememberMe.setChecked(true);
        }
        ShowMe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ShowMe.isChecked()) {
                    editPassword.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT);
                    editPassword.setTypeface(null, Typeface.NORMAL);
                    editPassword.setSelection(editPassword.getText().length());
                } else {
                    editPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                    editPassword.setTypeface(null, Typeface.NORMAL);
                    editPassword.setSelection(editPassword.getText().length());
                }
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editUserName.getText().toString().matches("")) {
                    Toast.makeText(ActivityLogin.this, "Please Enter Your User Name", Toast.LENGTH_SHORT).show();
                } else if (editPassword.getText().toString().matches("")) {
                    Toast.makeText(ActivityLogin.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                } else if (AppStatus.getInstance(ActivityLogin.this).isOnline()) {
                    DeviceToken = prefs.getString("refreshedToken", null);
                    progress = new ProgressDialog(ActivityLogin.this);
                    progress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                    progress.setIndeterminate(true);
                    //////
                    Drawable drawable = new ProgressBar(ActivityLogin.this).getIndeterminateDrawable();
                    drawable.setColorFilter(Color.rgb(250, 170, 50), PorterDuff.Mode.MULTIPLY);
                    progress.setIndeterminateDrawable(drawable);
                    ///////
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setMessage("Login please wait...");
                    progress.setCancelable(false);
                    progress.show();
                    AttemptLogin attemptLogin = new AttemptLogin();
                    attemptLogin.execute(editUserName.getText().toString(), editPassword.getText().toString());
                } else {
                    Toast.makeText(ActivityLogin.this, "Please check network connection and try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AppStatus.getInstance(ActivityLogin.this).isOnline()) {
                    new AsyncTask<String, String, JSONObject>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            ArrayList parametarGetAirline = new ArrayList();
                            parametarGetAirline.add(new BasicNameValuePair("status_id", "1"));
                            JSONObject json = jsonParser.makeHttpRequest(AppStatus.getbaseurl().baseurl() + "get_airlines", "POST", parametarGetAirline);
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
                                    Log.d("airlineResult", null);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute();
                    Intent intent = new Intent(getApplication(), ActivityRegistration.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplication(), ActivityRegistration.class);
                    startActivity(intent);
                }
            }
        });
        textForgotPass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
                builder.setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                forgetPass = editForgetPass.getText().toString();
                                new AsyncTask<String, String, JSONObject>() {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        ArrayList parametarGetAirline = new ArrayList();
                                        parametarGetAirline.add(new BasicNameValuePair("user_name", forgetPass));
                                        JSONObject json = jsonParser.makeHttpRequest(AppStatus.getbaseurl().baseurl() + "forgot_password", "POST", parametarGetAirline);
                                        return json;
                                    }

                                    protected void onPostExecute(JSONObject result) {
                                        try {
                                            if (result != null) {
                                                int status_id = result.getInt("status_id");
                                                String MSG = result.getString("status_msg");
                                                if (status_id != 0) {
                                                    Toast.makeText(ActivityLogin.this, MSG, Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(ActivityLogin.this, MSG, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                editForgetPass = new EditText(ActivityLogin.this);
                TextView title = new TextView(ActivityLogin.this);
                title.setText("Please enter your Username/Email");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.setView(editForgetPass);
                alert.show();
            }
        });
    }

    private class AttemptLogin extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String password = args[1];
            String name = args[0];
            ArrayList parametarLogin = new ArrayList();
            parametarLogin.add(new BasicNameValuePair("user_name", name));
            parametarLogin.add(new BasicNameValuePair("password", password));
            parametarLogin.add(new BasicNameValuePair("device_token", prefs.getString("refreshedToken", null)));
            parametarLogin.add(new BasicNameValuePair("device_type", "android"));
            JSONObject json = jsonParser.makeHttpRequest(AppStatus.getbaseurl().baseurl() + "user_login", "POST", parametarLogin);
            return json;
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    int status_id = result.getInt("status_id");
                    String MSG = result.getString("status_msg");
                    if (status_id != 0) {
                        JSONArray array = new JSONArray(result.getString("user_details"));
                        user_email = array.getJSONObject(0).getString("user_email");
                        fcm_id = array.getJSONObject(0).getString("fcm_id");
                        user_id = array.getJSONObject(0).getString("user_id");
                        user_name = array.getJSONObject(0).getString("user_name");
                        if (RememberMe.isChecked()) {
                            editor.putString("username", editUserName.getText().toString());
                            editor.putString("password", editPassword.getText().toString());
                            editor.putString("Rememberusername", editUserName.getText().toString());
                            editor.putString("Rememberpassword", editPassword.getText().toString());
                        } else {
                            editor.putString("username", editUserName.getText().toString());
                            editor.putString("password", editPassword.getText().toString());
                            editor.putString("Rememberusername", null);
                            editor.putString("Rememberpassword", null);
                        }
                        editor.putString("userId", user_id);
                        editor.putString("airlineTitle", array.getJSONObject(0).getString("airline_name"));
                        editor.putString("airlineId", array.getJSONObject(0).getString("airline_id"));
                        editor.putString("AirportId", array.getJSONObject(0).getString("base_airport_id"));
                        editor.putString("AirportCode", array.getJSONObject(0).getString("airport_code"));
                        editor.putString("drawerstart", "Start");
                        editor.putString("user_email", user_email);
                        editor.putString("fcm_id", fcm_id);
                        editor.commit();

                        if (fcm_id.equals("")) {
                            FcmRegistration();
                        }else {
                            progress.dismiss();
                            Intent intent = new Intent(getApplication(), ActivityMain.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), MSG, Toast.LENGTH_LONG).show();
                    }
                } else {
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                progress.dismiss();
                e.printStackTrace();
            }
        }
    }

    public void FcmRegistration() {

        firebaseAuth.createUserWithEmailAndPassword(user_email, "Simple@123")
                .addOnCompleteListener(ActivityLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            fcm_id = firebaseAuth.getCurrentUser().getUid();
                            UserData data = new UserData();
                            data.setemail(user_email);
                            data.setname(user_name);
                            data.setprofilePicLink("http://tripapi.trippusher.com/restAPIs/uploads/default_profile_pic.jpg");
                            usersRef.child(fcm_id).setValue(data);
                            editor.putString("fcm_id", fcm_id);
                            editor.commit();
                            new AsyncTask<String, String, JSONObject>() {
                                String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "update_fcm_id";

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                }

                                @Override
                                protected JSONObject doInBackground(String... args) {
                                    ArrayList parametarGetAirline = new ArrayList();
                                    parametarGetAirline.add(new BasicNameValuePair("user_id", user_id));
                                    parametarGetAirline.add(new BasicNameValuePair("fcm_id", fcm_id));
                                    JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                    return json;
                                }

                                protected void onPostExecute(JSONObject result) {
                                    try {
                                        if (result != null) {
                                            int status_id = result.getInt("status_id");
                                            String MSG = result.getString("status_msg");
                                            if (status_id != 0) {

                                                progress.dismiss();
                                                Intent intent = new Intent(getApplication(), ActivityMain.class);
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
                        }
                    }
                });

    }
    public static class UserData {
        private String email;
        private String name;
        private String profilePicLink;

        public UserData() {
            // empty constructor
        }

        public String getemail() {
            return email;
        }

        public void setemail(String emails) {
            email = emails;
        }

        public String getname() {
            return name;
        }

        public void setname(String names) {
            name = names;
        }

        public String getprofilePicLink() {
            return profilePicLink;
        }

        public void setprofilePicLink(String profilePicLinks) {
            profilePicLink = profilePicLinks;
        }

    }
}