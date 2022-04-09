package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.AnimalEvacutionAdapter;
import com.evacupet.model.NotificationModel;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.SessionManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Monil Naik on 28/03/2022.
 * MNTechnologies
 */
public class AnimalEvacutionActivity extends DashboardActivity {

    @BindView(R.id.rv_notification)
    RecyclerView rv_animals;
    @BindView(R.id.btn_evacuate_animal)
    Button btn_evacuate_animal;


    private static final String TAG = "AnimalEvacutionActivity";
    private ProgressDialogUtil progressDialogUtil;
    private ArrayList<NotificationModel> notificationModels;
    private AnimalEvacutionAdapter animalEvacutionAdapter;
    ParseObject notificationObject;
    ArrayList<NotificationModel> modelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.animals_to_evacuate));
        getLayoutInflater().inflate(R.layout.activity_notification, contentFrameLayout);
        ButterKnife.bind(this);
        btn_evacuate_animal.setVisibility(View.VISIBLE);
        if (getIntent().hasExtra(Constant.NOTIFICATION_OBJECT)) {
            notificationObject = getIntent().getParcelableExtra(Constant.NOTIFICATION_OBJECT);
        }
        init();


    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.showDialog();
        initializeAdapter();
        getAnimals();
        modelArrayList = new ArrayList<>();
        btn_evacuate_animal.setOnClickListener(v -> {
            boolean isDrop = false;
            modelArrayList =  new ArrayList<>();
            for (NotificationModel model : animalEvacutionAdapter.getList()) {
                if(model.isChecked()){
                    isDrop = true;
                    modelArrayList.add(model);
                }
            }
            if (!isDrop) {
                Toast.makeText(this, "Please select animal for drop", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, modelArrayList.size()+" animal for drop", Toast.LENGTH_SHORT).show();
                SendAnimalListForEvacuation();
            }
        });
    }

    private void initializeAdapter() {
        animalEvacutionAdapter = new AnimalEvacutionAdapter(this);
        rv_animals.setLayoutManager(new LinearLayoutManager(AnimalEvacutionActivity.this));
        rv_animals.setAdapter(animalEvacutionAdapter);
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    private void getAnimals(){
        notificationModels = new ArrayList<>();
        ParseObject Eventobject = notificationObject.getParseObject("Event");
//        double a = distance(Double.parseDouble(Eventobject.getString("lat")),
//                34.160801, Double.parseDouble(Eventobject.getString("lng")),-91.824785, 0,0);
//        Log.e(TAG, "Test::Result::Meter::New Method::"+a);
//        Log.e(TAG, "Test::Result::Miles::New Method::"+Math.round(metersToMiles(a)));

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Animals");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        //query.whereWithinMiles("GPS", new ParseGeoPoint(Double.parseDouble(Eventobject.getString("lat")),Double.parseDouble(Eventobject.getString("lng"))),20);
        query.whereWithinMiles("GPS", new ParseGeoPoint(0,0),20);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    progressDialogUtil.dismissDialog();
                    if (objects.size() > 0) {
                        Log.e(TAG,"Test=>>>Data Found=>"+objects.size());

                        for (ParseObject object1 : objects) {
                            notificationModels.add(new NotificationModel(object1));
                        }
                            new Handler().post(new Runnable(    ) {
                            @Override
                            public void run() {
                               //Update Anything
                                animalEvacutionAdapter.setData(notificationModels);
                            }
                        });
                    } else {
                        Log.e(TAG,"Test=>>>No Data Found");

                    }

                } else {
                    progressDialogUtil.dismissDialog();
                    Log.e(TAG,"error = "+ e.getMessage());
                }
            }
        });
    }

    private void SendAnimalListForEvacuation() {
        notificationModels = new ArrayList<>();
        ParseObject object = new ParseObject("EventOwnerAnimalRelation");
        object.put("animal", modelArrayList.get(0).getParseObject());
        object.put("event", notificationObject.getParseObject("Event"));
        object.put("owner", ParseUser.getCurrentUser());
        object.put("isPickedUp", 0);
        object.put("isDroppped", 0);

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) notifyAllVolunteer();
                else Toast.makeText(AnimalEvacutionActivity.this,"Something Went Wrong.",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void notifyAllVolunteer() {

    }

}
