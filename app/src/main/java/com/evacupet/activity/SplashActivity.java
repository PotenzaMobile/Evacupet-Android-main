package com.evacupet.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.evacupet.MyApp;
import com.evacupet.R;
import com.evacupet.model.NewsModel;
import com.evacupet.model.NotificationModel;
import com.evacupet.utility.BadgeUtils;
import com.evacupet.utility.SessionManager;
import com.google.gson.Gson;
import com.here.android.mpa.ftcr.FTCRRouteOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static int SPLASH_TIME_OUT = 3000;
    private boolean isLogin;
    public static int count  = 0;

    public static ArrayList<NewsModel> newsData = new ArrayList<NewsModel>();
    public static String aboutData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        isLogin = new SessionManager(this).isLogin();
        MyApp.savePreferenceLong(0);
        BadgeUtils.clearBadge(this);
        //getAllNews();
        //getAboutData();
        if(isLogin) {
            new SessionManager(this).updateUserDetails();
            StartActivity();
        }
        else StartActivity();
    }

    private void StartActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLogin) {
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);
    }
//    private void getUserDetails() {
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
//        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
//        query.findInBackground(new FindCallback<ParseObject>() {
//            public void done(List<ParseObject> objects, ParseException e) {
//                if (e == null) {
//                    if (objects.size() > 0) {
//                        for (ParseObject object1 : objects) {
//                            Log.e(TAG,"Is Volunteer?=>"+object1.getBoolean("Volunteer"));
//                            Gson gson = new Gson();
//                            String toStoreObject = gson.toJson(object1, .class);
//                        }
//                    } else {
//                        Log.e(TAG,"Test=>>>No Data Found");
//                    }
//                } else {
//                    Log.e(TAG,"error = "+ e.getMessage());
//                }
//                StartActivity();
//            }
//        });
//    }

    public ArrayList<NewsModel> getAllNews() {
        if(newsData.size()>0){
            newsData = new ArrayList<NewsModel>();
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("News");
        query.orderByDescending("createdAt")
                .whereEqualTo("status",0)
                .setLimit(50)
                .findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        int i=0;
                        //Collections.sort(objects, Collections.reverseOrder());
                        for (ParseObject object1 : objects) {
                            i++;
                            Date date = object1.getCreatedAt();
                            android.text.format.DateFormat df = new android.text.format.DateFormat();

                            Log.d("AAA:",object1.getString("title"));

                            newsData.add(new NewsModel(i,df.format("hh:mm a dd, MMM", date).toString(),object1.getString("title"),object1.getString("shortDescription"),object1.getString("description"),object1.getParseFile("image").getUrl()));
                        }
                    } else {

                    }

                } else {
                    //Log.e("error = ", e.getMessage());
                }
            }
        });
        return newsData;
    }

    public String getAboutData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Setting");
        query.orderByDescending("createdAt")
                .whereEqualTo("key","about")
                .setLimit(1)
                .findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (objects.size() > 0) {
                                aboutData = (String) objects.get(0).get("value");
                            } else {

                            }
                        } else {
                        }
                    }
                });
        return aboutData;
    }



}
