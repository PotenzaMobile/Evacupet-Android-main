package com.evacupet;

import android.app.Application;
import android.os.Debug;
import android.util.Log;

import com.evacupet.fcm.MyReceiver;
import com.google.firebase.iid.FirebaseInstanceId;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

public class MyApp extends Application {
    private static int count = 0;

    public static int getPreferenceInt() {
        return count;
    }

    public static void savePreferenceLong(int unOpenCount) {
        count = unOpenCount;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("3RtWqS6ALvhHJgFHCcWAoL1iQgi0GrTLZgGJlKNO")
                // if defined
                .clientKey("jfNdsuyUVkKmvcAYEYrb2KTaUdAVYAzGm46ICwt0")
                .server("https://parseapi.back4app.com")
                .enableLocalDataStore()
                .build()
        );
        ParsePush.subscribeInBackground("My Channel");
        sendToken();
    }

    private void sendToken() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        String token = FirebaseInstanceId.getInstance().getToken();
        //installation.setDeviceToken(installation.getDeviceToken());
        installation.setDeviceToken(token);
        //installation.put("GCMSenderId","522857416488"); //OLD ID
        installation.put("GCMSenderId","672382905123");
        //installation.setDeviceToken(android.provider.Settings.System.getString(super.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        if (ParseUser.getCurrentUser() != null){
            installation.put("user",ParseUser.getCurrentUser());
            installation.put("userID",ParseUser.getCurrentUser().getObjectId());
        }
        installation.saveInBackground();
    }


}
