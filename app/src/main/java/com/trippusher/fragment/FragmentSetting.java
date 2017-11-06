package com.trippusher.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.system.ErrnoException;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trippusher.ActivityCropImage;
import com.trippusher.AppStatus;
import com.trippusher.JSONParser;
import com.trippusher.Model;
import com.trippusher.R;
import com.trippusher.activity.ActivityChangeAirline;
import com.trippusher.activity.ActivityChangeBase;
import com.trippusher.activity.ActivityChangePass;
import com.trippusher.activity.ActivityLogin;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 * Created by Desktop-KS on 7/26/2017.
 */

public class FragmentSetting extends Fragment {
    JSONParser jsonParser = new JSONParser();
    Fragment fragment;
    private Bitmap b;
    TextView Header, airlineTitle, user_name, user_mobile, PAirline, PPassword, user_Base, txtPFirstName, txtPLastName, NoNet;
    LinearLayout layViewProfile;
    String mobile, userId, AirlineTitle, AirlineId, AirportId, cPassword, AirportCode, dp, notification_status, updated_airline_date;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    EditText CurrentPassword, ChagePhone;
    ImageView profilePic;
    Switch SwtTripNotification, SwtMessageNotification;
    RelativeLayout RelativeLayoutData;
    private Uri mCropImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = prefs.edit();
        userId = prefs.getString("userId", null);
        AirlineTitle = prefs.getString("airlineTitle", null);
        AirlineId = prefs.getString("airlineId", null);
        AirportId = prefs.getString("AirportId", null);
        AirportCode = prefs.getString("AirportCode", null);
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        NoNet = (TextView) view.findViewById(R.id.txtNoNet);
        layViewProfile = (LinearLayout) view.findViewById(R.id.layViewProfile);
        Header = (TextView) view.findViewById(R.id.txtPHeader);
        airlineTitle = (TextView) view.findViewById(R.id.txtPAirline);
        txtPFirstName = (TextView) view.findViewById(R.id.txtPFirstName);
        profilePic = (ImageView) view.findViewById(R.id.profilePic);
        txtPLastName = (TextView) view.findViewById(R.id.txtPLastName);
        user_name = (TextView) view.findViewById(R.id.txtPUserName);
        user_Base = (TextView) view.findViewById(R.id.txtPUserBase);
        user_mobile = (TextView) view.findViewById(R.id.txtPMobile);
        PAirline = (TextView) view.findViewById(R.id.txtPAirline);
        PPassword = (TextView) view.findViewById(R.id.txtPPassword);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SwtTripNotification = (Switch) view.findViewById(R.id.SwtTripNotification);
        SwtMessageNotification = (Switch) view.findViewById(R.id.SwtMessageNotification);
        RelativeLayoutData = (RelativeLayout) view.findViewById(R.id.LayProfileData);
        Header.setText("Setting");
        airlineTitle.setText(AirlineTitle);
        user_Base.setText(AirportCode);
        if (AppStatus.getInstance(getContext()).isOnline()) {
            new AsyncTask<String, String, JSONObject>() {
                String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "get_user_details";

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected JSONObject doInBackground(String... args) {
                    ArrayList parametarGetAirline = new ArrayList();
                    parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
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
                                JSONArray array = new JSONArray(result.getString("user_details"));
                                dp = array.getJSONObject(0).getString("user_image");
                                if (array.getJSONObject(0).getString("user_image") != "") {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            try {
                                                InputStream in = new URL("http://tripapi.trippusher.com/restAPIs/uploads/" + dp).openStream();
                                                b = BitmapFactory.decodeStream(in);
                                            } catch (Exception e) {
                                                // log error
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void result) {
                                            if (b != null) {
                                                profilePic.setImageBitmap(b);
                                            } else {
                                                profilePic.setImageResource(R.drawable.userc);
                                            }
                                        }
                                    }.execute();
                                } else {
                                    profilePic.setImageResource(R.drawable.userc);
                                }
                                if (array.getJSONObject(0).getString("notification_status").matches(String.valueOf(1))) {
                                    SwtTripNotification.setChecked(true);
                                } else {
                                    SwtTripNotification.setChecked(false);
                                }
                                if (array.getJSONObject(0).getString("message_status").matches(String.valueOf(1))) {
                                    SwtMessageNotification.setChecked(true);
                                } else {
                                    SwtMessageNotification.setChecked(false);
                                }
                                txtPFirstName.setText(array.getJSONObject(0).getString("first_name"));
                                txtPLastName.setText(array.getJSONObject(0).getString("last_name"));
                                user_name.setText(array.getJSONObject(0).getString("user_name"));
                                String numStr = array.getJSONObject(0).getString("phone_no");
                                numStr = "(" + numStr.substring(0, 3) + ") " + numStr.substring(3, 6) + "-" + numStr.substring(6);
                                user_mobile.setText(numStr);
                                long timestampString = Long.parseLong(array.getJSONObject(0).getString("updated_airline_date"));
                                updated_airline_date = new java.text.SimpleDateFormat("MM/dd/yyyy").
                                        format(new java.util.Date(timestampString * 1000));
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
            }.execute();
        } else {
            Toast.makeText(getContext(), "Please check network connection and try again", Toast.LENGTH_LONG).show();
        }

        PAirline.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("You may only switch airlines once every 30 days. Are you sure you want to continue?");
                builder.setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Date expiryDate = null;
                                Calendar c = Calendar.getInstance();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                                try {
                                    Date myDate = dateFormat.parse(updated_airline_date);
                                    c.setTime(myDate);
                                    c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 31);
                                    expiryDate = c.getTime();
                                    Date currentDate = new Date();
                                    c.setTime(currentDate);
                                    int dayOfcurrentDate = c.get(Calendar.DAY_OF_YEAR);
                                    c.setTime(expiryDate);
                                    int dayOfexpiryDate = c.get(Calendar.DAY_OF_YEAR);
                                    int diff = (dayOfexpiryDate - dayOfcurrentDate);
                                    if (expiryDate.before(currentDate)) {
                                        Intent i = new Intent(getContext(), ActivityChangeAirline.class);
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(getActivity(), "You changed your airline before " + String.valueOf(diff) + " days", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                TextView title = new TextView(getContext());
                title.setText("Warning");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.show();
            }
        });
        user_Base.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cPassword = CurrentPassword.getText().toString();
                                new AsyncTask<String, String, JSONObject>() {
                                    String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "validate_user_password";

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected JSONObject doInBackground(String... args) {
                                        ArrayList parametarGetAirline = new ArrayList();
                                        parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                                        parametarGetAirline.add(new BasicNameValuePair("password", cPassword));
                                        JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                        return json;
                                    }

                                    protected void onPostExecute(JSONObject result) {
                                        try {
                                            if (result != null) {
                                                int status_id = result.getInt("status_id");
                                                String MSG = result.getString("status_msg");
                                                if (status_id != 0) {
                                                    Intent refresh = new Intent(getContext(), ActivityChangeBase.class);
                                                    startActivity(refresh);
                                                } else {
                                                    Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
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
                CurrentPassword = new EditText(getContext());
                CurrentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                TextView title = new TextView(getContext());
                title.setText("Current password");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.setView(CurrentPassword);
                alert.show();
            }
        });
        PPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cPassword = CurrentPassword.getText().toString();
                        new AsyncTask<String, String, JSONObject>() {
                            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "validate_user_password";

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected JSONObject doInBackground(String... args) {
                                ArrayList parametarGetAirline = new ArrayList();
                                parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                                parametarGetAirline.add(new BasicNameValuePair("password", cPassword));
                                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                return json;
                            }

                            protected void onPostExecute(JSONObject result) {
                                try {
                                    if (result != null) {
                                        int status_id = result.getInt("status_id");
                                        String MSG = result.getString("status_msg");
                                        if (status_id != 0) {
                                            editor.putString("old_password", cPassword);
                                            editor.commit();
                                            Intent refresh = new Intent(getContext(), ActivityChangePass.class);
                                            startActivity(refresh);
                                        } else {
                                            Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
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
                CurrentPassword = new EditText(getContext());
                CurrentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                TextView title = new TextView(getContext());
                title.setText("Current password");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.setView(CurrentPassword);
                alert.show();
            }
        });
        user_mobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to change mobile number");
                builder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mobile = ChagePhone.getText().toString();

                                        new AsyncTask<String, String, JSONObject>() {
                                            String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "update_phone_number";

                                            @Override
                                            protected void onPreExecute() {
                                                super.onPreExecute();
                                            }

                                            @Override
                                            protected JSONObject doInBackground(String... args) {
                                                ArrayList parametarGetAirline = new ArrayList();
                                                String mobil = mobile.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                                                parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                                                parametarGetAirline.add(new BasicNameValuePair("phone_no", mobil));
                                                JSONObject json = jsonParser.makeHttpRequest(GetAirlineURL, "POST", parametarGetAirline);
                                                return json;
                                            }

                                            protected void onPostExecute(JSONObject result) {
                                                try {
                                                    if (result != null) {
                                                        int status_id = result.getInt("status_id");
                                                        String MSG = result.getString("status_msg");
                                                        if (status_id != 0) {
                                                            Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
                                                            fragment = new FragmentSetting();
                                                            FragmentTransaction tr = getFragmentManager().beginTransaction();
                                                            tr.replace(R.id.fragment_container, fragment);
                                                            tr.commit();
                                                        } else {
                                                            Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
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
                        ChagePhone = new EditText(getContext());
                        ChagePhone.addTextChangedListener(new Model.PhoneNumberFormattingTextWatcher(ChagePhone));
                        ChagePhone.setInputType(InputType.TYPE_CLASS_NUMBER);
                        ChagePhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
                        TextView title = new TextView(getContext());
                        title.setText("New Mobile Number");
                        title.setPadding(10, 10, 10, 10);
                        title.setGravity(Gravity.CENTER);
                        title.setTextColor(Color.BLACK);
                        alert.setCustomTitle(title);
                        alert.setView(ChagePhone);
                        alert.show();
                    }
                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                TextView title = new TextView(getContext());
                title.setText("Warning");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                alert.setCustomTitle(title);
                alert.show();
            }
        });
        SwtTripNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SwtTripNotification.isChecked()) {
                    notification_status = "1";
                } else {
                    notification_status = "0";
                }
                new AsyncTask<String, String, JSONObject>() {
                    String GetAirlineURL = AppStatus.getbaseurl().baseurl() + "update_profile_notification";

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected JSONObject doInBackground(String... args) {
                        ArrayList parametarGetAirline = new ArrayList();
                        parametarGetAirline.add(new BasicNameValuePair("user_id", userId));
                        parametarGetAirline.add(new BasicNameValuePair("status", notification_status));
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
                                    Toast.makeText(getContext(), MSG, Toast.LENGTH_LONG).show();
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
                }.execute();
            }
        });
        profilePic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*Intent intent = new Intent(v.getContext(), ActivityCropImage.class);
                startActivity(intent);*/
                startActivityForResult(getPickImageChooserIntent(), 200);
            }
        });

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = getPickImageResultUri(data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            boolean requirePermissions = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    isUriRequiresPermissions(imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            if (!requirePermissions) {
                //mCropImageView.setImageUriAsync(imageUri);
                Intent intent = new Intent(this.getContext(), ActivityCropImage.class);
                intent.putExtra("imageUri", imageUri.toString());
                //intent.PutExtra("scale", true);
                startActivity(intent);
            }
        }
    }
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getContext().getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getContext().getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }
    public boolean isUriRequiresPermissions(Uri uri) {
        try {
            ContentResolver resolver = getContext().getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            stream.close();
            return false;
        } catch (FileNotFoundException e) {
            if (e.getCause() instanceof ErrnoException) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}