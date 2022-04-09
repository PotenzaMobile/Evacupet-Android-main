package com.evacupet.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;


import com.evacupet.activity.LoginActivity;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class SessionManager {
    public static final String TAG = SessionManager.class.getSimpleName();
    private Context context;private SharedPreferences.Editor appPrefEditor;
    private static final String APP_PREF_NAME = "Evacuet";
    private SharedPreferences appPref;


    public SessionManager(Context context) {
        this.context = context;
        appPref = context.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
        appPrefEditor = appPref.edit();
        appPrefEditor.apply();
    }

    public void saveUserDetail(String userName, String sessionToken) {
        appPrefEditor.putString(Constant.USERNAME, userName);
        appPrefEditor.putString(Constant.TOKEN, sessionToken);
        appPrefEditor.putBoolean(Constant.IS_LOGIN, true);
        appPrefEditor.commit();
    }

    public void setIsLogin() {
        appPrefEditor.putBoolean(Constant.IS_LOGIN, true);
        appPrefEditor.commit();
    }

    public boolean isLogin() {
        return appPref.getBoolean(Constant.IS_LOGIN, false);
    }

    public String getUserId() {
        return appPref.getString(Constant.TOKEN, "");
    }


    public void logout(String msg) {
        logoutAlert(msg);

    }

    private void sentToLoginScreen() {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void logoutAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
       // builder.setMessage("Are you sure to logout?");
        builder.setMessage(msg);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        ParseUser.logOutInBackground(new LogOutCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    // logOutSuccessful();
                                    appPrefEditor.clear();
                                    appPrefEditor.commit();
                                    sentToLoginScreen();
                                } else {
                                    //  somethingWentWrong();
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                            }
                        });
                    }
                });


        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    public void closeApp(){

    }


    public void updateUserDetails() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object1 : objects) {
                            appPrefEditor.putBoolean(Constant.USER_IS_VOLUNTEER, object1.getBoolean("Volunteer"));
                            appPrefEditor.commit();
                        }
                    } else {
                        Log.e(TAG,"Test=>>>No Data Found");
                    }
                } else {
                    Log.e(TAG,"error = "+ e.getMessage());
                }
            }
        });


    }
    public void setBoolean(String Key,boolean value){appPrefEditor.putBoolean(Key, value);}
    public boolean getBoolean(String Key){
        return appPref.getBoolean(Key, false);
    }

    public void setString(String Key,String value){appPrefEditor.putString(Key, value);}
    public String getString(String Key){return appPref.getString(Key, "");}



}
