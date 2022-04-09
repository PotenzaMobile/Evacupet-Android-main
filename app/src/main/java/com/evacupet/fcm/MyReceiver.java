package com.evacupet.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.evacupet.MyApp;
import com.evacupet.R;
import com.evacupet.activity.AreaUnderEvacupetionActivity;
import com.evacupet.activity.HomeActivity;
import com.evacupet.utility.BadgeUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class MyReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = "PUSH_NOTIF";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.e(TAG, "onPushOpen triggered!");
        Log.e(TAG, "pushOpen = " + intent.getExtras().toString());

        JSONObject pushData;
        String alert = null;
        String title = null;
        String shareLink = null;
        try {
            pushData = new JSONObject(intent.getStringExtra(MyReceiver.KEY_PUSH_DATA));
            Log.e(TAG, "notification = " + pushData.toString());

            alert = pushData.getString("alert");
            shareLink = pushData.getString("ShareLink");
            Log.e("ShareLink = ",shareLink);
            title = pushData.getString("title");

        } catch (JSONException e) {
        }

        if (alert.equals("Message: There is an emergency in your area.  You may need to evacuate your animals.  Please open Evac-U-Pet to proceed.")) {

            Intent i = new Intent(context, AreaUnderEvacupetionActivity.class);
            i.putExtras(intent.getExtras());
            i.putExtra("shareLink",shareLink);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else if (alert.equals("EvacuPet")) {
            Intent i = new Intent(context, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else {
            Intent i = new Intent(context, HomeActivity.class);
            i.putExtra("Notification", "Notification");
            i.putExtra("msg", alert);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    @Override
    public void onPushReceive(Context context, Intent intent) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.increment("badge");
        installation.saveInBackground();
        Log.e(TAG, "onPushReceive triggered!");
        JSONObject pushData;
        String alert = null;
        String title = null;
        try {
            pushData = new JSONObject(intent.getStringExtra(MyReceiver.KEY_PUSH_DATA));
            Log.e(TAG, "notification = " + pushData.toString());
            alert = pushData.getString("alert");
            title = pushData.getString("title");

        } catch (JSONException e) {
        }

        Log.i(TAG, "alert is " + alert);
        Log.i(TAG, "title is " + title);


        Intent cIntent = new Intent(MyReceiver.ACTION_PUSH_OPEN);
        cIntent.putExtras(intent.getExtras());
        cIntent.setPackage(context.getPackageName());

        PendingIntent pContentIntent =
                PendingIntent.getBroadcast(context, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      /*  NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.drawable.elogo)
                .setContentTitle(alert)
                .setContentText(title)
                .setContentIntent(pContentIntent)
                .setAutoCancel(true);


        NotificationManager myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        myNotificationManager.notify(1, builder.build());
*/
        /////
        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/notification");
        final int icon = R.drawable.ic_logo_icon;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(title);
        String CHANNEL_ID = "My Channel";// The id of the channel.
        CharSequence name = context.getString(R.string.channel_name);
        Notification notification;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(alert)
                .setContentIntent(pContentIntent)
                .setSound(alarmSound)
                .setStyle(inboxStyle)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setContentText(title)
                .setChannelId(CHANNEL_ID)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        int unOpenCount= MyApp.getPreferenceInt();
        unOpenCount=unOpenCount+1;

        MyApp.savePreferenceLong(unOpenCount);

        notificationManager.notify(unOpenCount, notification);
        BadgeUtils.setBadge(context,(int)unOpenCount);
    }
}