package com.trippusher.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.trippusher.R;
import com.trippusher.activity.ActivityChat;
import com.trippusher.activity.ActivityLogin;
import com.trippusher.activity.ActivityTripDetail;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Desktop-KS on 8/1/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String data, title, body, Type;
    Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        final Map<String, String> map = new HashMap<String, String>(remoteMessage.getData());
        if (map.containsKey("title")) {
            title = map.get("title");
        }
        if (map.containsKey("body")) {
            body = map.get("body");
            //body = StringEscapeUtils.unescapeJava(map.get("body"));
        }
        if (map.containsKey("data")) {
            data = map.get("data");
            String[] separated = data.split(":");
            Type = separated[0].trim();
            if (Type.equals("u_id")) {
                if (isAppOnForeground() == false) {
                    String[] separated1 = data.split(",");
                    String[] separated2 = separated1[0].trim().split(":");
                    String[] separated3 = separated1[1].trim().split(":");
                    sendMessageNotification(body, title, separated2[1].trim(), separated3[1].trim());
                }
            }
            if (Type.equals("t_id")) {
                sendNotification(body, title, separated[1].trim());
            }
        }
    }

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = getApplicationContext().getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                String mPackageName = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                if (mPackageName.equals("com.trippusher.activity.ActivityChat")) {
                    //Log.d("CURRENT Activity ::" ,mPackageName);
                    return true;
                }
            }
        }
        return false;
    }

    private void sendNotification(String messageBody, String Title, String Id) {
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        if (username != null && password != null) {
            intent = new Intent(this, ActivityTripDetail.class);
            intent.putExtra("postTripId", Id);
        } else {
            intent = new Intent(this, ActivityLogin.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(Title)
                .setContentText(messageBody)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendMessageNotification(String messageBody, String Title, String senderUID, String Id) {
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        if (username != null && password != null) {
            Bitmap b = null;
            intent = new Intent(this, ActivityChat.class);
            intent.putExtra("ChatLocation", Id);
            intent.putExtra("ReceiverFcmId", senderUID);
            intent.putExtra("BitmapImage", b);
        } else {
            intent = new Intent(this, ActivityLogin.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        //int icon=R.drawable.icon;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(Title)
                .setContentText(StringEscapeUtils.unescapeJava(messageBody))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){

            notificationBuilder.setSmallIcon(R.drawable.notificationicon);
            notificationBuilder.setColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }
        else {notificationBuilder.setSmallIcon(R.drawable.ic_launcher);}

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_launcher : R.drawable.icon;
        //.setLargeIcon((BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)));
    }
}