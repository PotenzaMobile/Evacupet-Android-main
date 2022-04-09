package com.evacupet.activity;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;

import com.evacupet.adapter.EvacuationCenterAdapter;
import com.evacupet.interfaceHelper.EvacuationCenterClick;
import com.evacupet.model.AnimalListModel;

import com.evacupet.model.EvacuationCenterListModel;

import com.evacupet.utility.Constant;
import com.evacupet.utility.LocationOnUtility;
import com.evacupet.utility.ProgressDialogUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONObject;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvacuationCenterListActivity extends DashboardActivity implements EvacuationCenterClick {
    private ProgressDialogUtil progressDialogUtil;
    private static final String TAG = EvacuationCenterListActivity.class.getSimpleName();
    @BindView(R.id.rv_center_location)
    RecyclerView rvCenterLocation;
    private EvacuationCenterAdapter adapter;
    private double lat, lnge;
    private LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private ArrayList<AnimalListModel> modelArrayList;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private int catCapacity = 0, horseCapacity = 0, cowCapacity = 0, sheepCapacity = 0, pigCapacity = 0, goatCapacity = 0, poultryCapacity = 0, dogCapacity = 0, reptileCapacity = 0, birdCapacity = 0, pocketPetCapacity = 0, rabbitCapacity = 0, otherCapacity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.evacuation_centers));
        getLayoutInflater().inflate(R.layout.activity_evacuation_center_list, contentFrameLayout);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        setAdapter();
        if (getIntent().hasExtra(Constant.ANIMAL_DATA)) {
            modelArrayList = getIntent().getParcelableArrayListExtra(Constant.ANIMAL_DATA);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (new LocationOnUtility(this, this).enableLocation()) {
            if (lat == 0.0) {
                trackLocation();
            } else {
                if (mFusedLocationProviderClient != null && mLocationRequest != null) {
                    mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
            }
        }
    }


    @Override
    public void onImageSuccess(Intent data) {

    }

    private void setAdapter() {
        adapter = new EvacuationCenterAdapter(this, this);
        rvCenterLocation.setLayoutManager(new LinearLayoutManager(this));
        rvCenterLocation.setAdapter(adapter);
    }

    private void checkCapacity() {
        catCapacity = 0;
        horseCapacity = 0;
        cowCapacity = 0;
        sheepCapacity = 0;
        pigCapacity = 0;
        goatCapacity = 0;
        poultryCapacity = 0;
        dogCapacity = 0;
        reptileCapacity = 0;
        birdCapacity = 0;
        pocketPetCapacity = 0;
        rabbitCapacity = 0;
        otherCapacity = 0;
        if (modelArrayList != null && modelArrayList.size() > 0) {
            for (AnimalListModel model : modelArrayList) {
                ParseObject object = model.getParseObject();
                String species = object.getString("Species");
                Log.e(TAG, "species = " + species);
                if (!TextUtils.isEmpty(species)) {
                    switch (species) {
                        case Constant.HORSE:
                            horseCapacity++;
                            break;
                        case Constant.BIRD:
                            birdCapacity++;
                            break;
                        case Constant.DOG:
                            dogCapacity++;
                            break;
                        case Constant.CAT:
                            catCapacity++;
                            break;
                        case Constant.REPTILE:
                            reptileCapacity++;
                            break;
                        case Constant.GOATS:
                            goatCapacity++;
                            break;
                        case Constant.SHEEP:
                            sheepCapacity++;
                            break;
                        case Constant.POCKET_PET:
                            pocketPetCapacity++;
                            break;
                        case Constant.RABBIT:
                            rabbitCapacity++;
                            break;
                        case Constant.OTHER:
                            otherCapacity++;
                            break;
                        case Constant.COW:
                            cowCapacity++;
                            break;
                        case Constant.PIG:
                            pigCapacity++;
                            break;
                        case Constant.POULTRY:
                            poultryCapacity++;
                            break;
                    }
                }

            }

            checkCapacityApi();

        }
    }

    private void doneLoadAnimal(final String centerId, final String lat, final String myLong) {
        for (AnimalListModel model : modelArrayList) {
            ParseObject object = model.getParseObject();
            progressDialogUtil.showDialog();
            object.put("EvacuatedBy", ParseUser.getCurrentUser());
            object.put("Status", 2);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });

        }
        Log.e("bgfgh = ",lat+"jfgd=="+myLong);
        Intent intent = new Intent(this, EvacuationProgressActivity.class);
        intent.putExtra(Constant.FOUND_LAT, Double.parseDouble(lat));
        intent.putExtra(Constant.FOUND_LONG, Double.parseDouble(myLong));
        intent.putExtra(Constant.FOUND_LOCATION, centerId);
        startActivity(intent);
    }

    private void checkCapacityApi() {
        progressDialogUtil.showDialog();
        AndroidNetworking.post("https://evacu.pet/alert-now/GetEvacutaionCenterByLatLong.php")
                .addBodyParameter("lat", String.valueOf(lat))
                .addBodyParameter("lon", String.valueOf(lnge))
                .addBodyParameter("CatCapacity", String.valueOf(catCapacity))
                .addBodyParameter("PigCapacity", String.valueOf(pigCapacity))
                .addBodyParameter("CowCapacity", String.valueOf(cowCapacity))
                .addBodyParameter("HorseCapacity", String.valueOf(horseCapacity))
                .addBodyParameter("GoatCapacity", String.valueOf(goatCapacity))
                .addBodyParameter("DogCapacity", String.valueOf(dogCapacity))
                .addBodyParameter("BunnyCapacity", String.valueOf(rabbitCapacity))
                .addBodyParameter("ChickenCapacity", String.valueOf(poultryCapacity))
                .addBodyParameter("SheepCapacity", String.valueOf(sheepCapacity))
                .addBodyParameter("Other", String.valueOf(otherCapacity))
                .addBodyParameter("Reptile", String.valueOf(reptileCapacity))
                .addBodyParameter("Bird", String.valueOf(birdCapacity))
                .addBodyParameter("Pocketpet", String.valueOf(pocketPetCapacity))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "check  = " + response.toString());
                        String status = response.optString("Result");
                        if (status.equals("Result Found")) {
                            try {
                                JSONArray jsonArray = response.optJSONArray("center");
                                GsonBuilder gsonBuilder = new GsonBuilder();
                                Gson gson = gsonBuilder.create();
                                Type listType = new TypeToken<List<EvacuationCenterListModel>>() {
                                }.getType();
                                ArrayList<EvacuationCenterListModel> evacuationCenterListModels = gson.fromJson(String.valueOf(jsonArray), listType);
                                if (evacuationCenterListModels != null && !evacuationCenterListModels.isEmpty()) {
                                    adapter.setData(evacuationCenterListModels);
                                    adapter.notifyDataSetChanged();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }
                        } else {
                            Toast.makeText(EvacuationCenterListActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, anError.getMessage());

                    }
                });
    }

    @SuppressLint({"MissingPermission"})
    private void trackLocation() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                }
            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000);
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(Task<Location> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        Location newLocation = task.getResult();
                        Log.e("MapsActivity", "Location: " + newLocation.getLatitude() + " " + newLocation.getLongitude());

                        lat = newLocation.getLatitude();
                        lnge = newLocation.getLongitude();
                        checkCapacity();
                        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                    } else {
                        trackLocation();
                    }
                }
            }
        });

    }

    @Override
    public void itemClick(String centerId, String lat, String myLong) {
        doneLoadAnimal(centerId, lat, myLong);

    }
}