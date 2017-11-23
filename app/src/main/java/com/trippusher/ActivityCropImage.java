package com.trippusher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.trippusher.activity.ActivityMain;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class ActivityCropImage extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private Bitmap bitmap;
    String ServerUploadPath = AppStatus.getbaseurl().baseurl() + "upload_profile_image";
    String image_name,UserId,fcm_id;
    private CropImageView mCropImageView;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    JSONParser jsonParser = new JSONParser();
    private DatabaseReference usersRef;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("new_user");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        UserId = prefs.getString("userId", null);
        fcm_id = prefs.getString("fcm_id", null);
        Bundle bundle = getIntent().getExtras();
        //Log.d("uri",bundle.getString("imageUri"));
        if(bundle.getString("imageUri")!= null)
        {
            Log.d("uri1",bundle.getString("imageUri"));
            mCropImageView.setImageUriAsync(Uri.parse(bundle.getString("imageUri")));
        }
    }
    public void onCropImageClick(View view)
    {
        bitmap = mCropImageView.getCroppedImage(500, 500);
        postComment();
    }
    public void onBackClick(View view)
    {
        finish();
    }
    public void postComment() {
        progressDialog = ProgressDialog.show(ActivityCropImage.this,"Image is Uploading","Please Wait",false,false);
        File filesDir = getFilesDir();
        File imageFile = new File(filesDir, (ActivityCropImage.this) + "_" + Math.random() + ".jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        Map<String, String> params = new HashMap<String, String>();
        //params.put("user_id", "432");
        //params.put("caption", "hlo");
        //params.put("privacy", "ghfhf");
        //params.put("cur_date", "353453535334");


        CustomMultipartRequest requestPost = new CustomMultipartRequest(ServerUploadPath, params, imageFile, "file.jpg", "file",
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Toast.makeText(ActivityCropImage.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("error", volleyError.getMessage());
                    }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONObject jObject = new JSONObject(response);
                            if (jObject.getString("status_id").equals("1")) {
                                //Log.e("responce....::>>>", response);
                                image_name = jObject.getString("image_name");
                                usersRef.child(fcm_id).child("profilePicLink").setValue("http://tripapi.trippusher.com/restAPIs/uploads/"+image_name);
                                new AsyncTask<String, String, JSONObject>() {
                                    String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "update_profile_image/";

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        ArrayList parametarGetAirport = new ArrayList();
                                        parametarGetAirport.add(new BasicNameValuePair("user_id", UserId));
                                        parametarGetAirport.add(new BasicNameValuePair("user_image", image_name));
                                        JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirport);
                                        return json;
                                    }

                                    protected void onPostExecute(JSONObject result) {
                                        try {
                                            if (result != null) {
                                                int status_id = result.getInt("status_id");
                                                String MSG = result.getString("status_msg");
                                                if (status_id != 0) {
                                                    progressDialog.dismiss();
                                                    Intent i =new Intent(getApplicationContext(), ActivityMain.class);
                                                    //editor.putString("ChangeProfile", "ChangeProfile");
                                                    //editor.commit();
                                                    i.putExtra("ChangeProfile", "ChangeProfile");
                                                    finish();
                                                    startActivity(i);
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
                                Toast.makeText(ActivityCropImage.this, "no data available", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        requestPost.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue postqueue = Volley.newRequestQueue(ActivityCropImage.this);
        postqueue.add(requestPost);
    }
}
