package com.evacupet.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.NotificationAdapter;
import com.evacupet.model.NotificationModel;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.SessionManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationActivity extends DashboardActivity {

    @BindView(R.id.rv_notification)
    RecyclerView rv_notification;

    private static final String TAG = "NotificationActivity";
    private ProgressDialogUtil progressDialogUtil;
    private ArrayList<NotificationModel> notificationModels;
    private NotificationAdapter notificationAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.notification));
        getLayoutInflater().inflate(R.layout.activity_notification, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.showDialog();
        initializeAdapter();
        if(new SessionManager(this).getBoolean(Constant.USER_IS_VOLUNTEER)) getVolunteer_Notification();
        else getOwner_Notification();
    }

    private void initializeAdapter() {
        notificationAdapter = new NotificationAdapter(this);
        rv_notification.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
        rv_notification.setAdapter(notificationAdapter);
    }

    private void getOwner_Notification(){
         notificationModels = new ArrayList<>();
         ParseQuery<ParseObject> query = ParseQuery.getQuery("OwnerEventPushLog");
         query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        Log.e(TAG,"User=>>>"+ParseUser.getCurrentUser().getUsername());

         query.findInBackground(new FindCallback<ParseObject>() {
             public void done(List<ParseObject> objects, ParseException e) {
                 if (e == null) {
                     progressDialogUtil.dismissDialog();
                     if (objects.size() > 0) {
                         for (ParseObject object1 : objects) {
                             notificationModels.add(new NotificationModel(object1));
                         }
                         new Handler().post(new Runnable() {
                             @Override
                             public void run() {
                                 notificationAdapter.setData(notificationModels);
                                 getEventStatusForOwner();
                             }
                         });
                     } else {
                         Log.e(TAG,"Test=>>>No Data Found");

                     }

                 } else {
                     progressDialogUtil.dismissDialog();
                     Log.e(TAG,"Error = "+ e.getMessage());
                 }
             }
         });
     }

    private void getVolunteer_Notification(){
        notificationModels = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventVolunteer");
        query.whereEqualTo("volunteer", ParseUser.getCurrentUser());
        query.orderByDescending("status");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    progressDialogUtil.dismissDialog();
                    if (objects.size() > 0) {
                        for (ParseObject object1 : objects) {
                            notificationModels.add(new NotificationModel(object1));
                        }
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                notificationAdapter.setData(notificationModels);
                            }
                        });
                    } else {
                        Log.e(TAG,"No Data Found");
                    }
                } else {
                    progressDialogUtil.dismissDialog();
                    Log.e(TAG,"error = "+ e.getMessage());
                }
            }
        });
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    private void getEventStatusForOwner() {
        progressDialogUtil.showDialog();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventVolunteer");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialogUtil.dismissDialog();
                if (e == null) {
                    if (objects.size() > 0) {
                        Log.e(TAG,"Test=>>>Data Found=>"+objects.size());

                        for (ParseObject object1 : objects) {

                            for (int i=0;i<notificationModels.size(); i++){
                                if(notificationModels.get(i).getParseObject().getParseObject("Event").equals(object1.getParseObject("Event"))){

                                    switch(object1.getInt("status")) {
                                        case 0: {
                                            notificationModels.get(i).setStatus("Pending");
                                        }
                                        case 1: {
                                            notificationModels.get(i).setStatus("Accept");
                                        }
                                        case 2: {
                                            notificationModels.get(i).setStatus("Reject");
                                        }
                                    }
                                }
                            }
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
}