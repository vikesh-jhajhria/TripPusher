package com.trippusher.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Desktop-KS on 8/1/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private SharedPreferences prefs;

    @Override
    public void onTokenRefresh() {
        //Getting registration token
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Displaying token on logcat
        editor.putString("refreshedToken", refreshedToken);
        editor.commit();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }
}