package com.evacupet.utility;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.evacupet.model.NotificationModel;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monil Naik on 05/04/2022.
 * MNTechnologies
 */
public class ParseHelper {
    private static final String TAG = "PassData";
    private ProgressDialogUtil progressDialogUtil;

    Context context;
    private Activity mActivity;

    private ArrayList<NotificationModel> notificationModels;


    private static ParseObject eventVolunteer_Object;


    public ParseHelper(Context context, Activity mActivity) {
        this.context = context;
        this.mActivity = mActivity;
    }

    private void getEventStatus(){
        progressDialogUtil = new ProgressDialogUtil(mActivity);
        progressDialogUtil.showDialog();
        notificationModels = new ArrayList<>();
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery("EventVolunteer");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        Log.e(TAG,"User=>>>"+ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<com.parse.ParseObject>() {
            public void done(List<com.parse.ParseObject> objects, ParseException e) {
                if (e == null) {
                    progressDialogUtil.dismissDialog();
                    if (objects.size() > 0) {
                        for (com.parse.ParseObject object1 : objects) {
                            notificationModels.add(new NotificationModel(object1));
                        }
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                new SessionManager(context).setString(Constant.EVENT_VOLUNTEER,"");
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
}
