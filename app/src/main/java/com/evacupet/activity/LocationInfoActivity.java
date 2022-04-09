package com.evacupet.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.utility.BaseUtility;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationInfoActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = LocationInfoActivity.class.getSimpleName();
    @BindView(R.id.tv_first_name)
    TextView tvFirstName;
    @BindView(R.id.tv_last_name)
    TextView tvLastName;
    @BindView(R.id.tv_email)
    TextView tvEmail;
    @BindView(R.id.tv_mobile)
    TextView tvMobile;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.tv_city)
    TextView tvCity;
    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_zipcode)
    TextView tvZipcode;
    @BindView(R.id.tv_property_notes)
    TextView tvPropertyNotes;
    @BindView(R.id.btn_load_animal)
    Button btnLoadAnimal;
    @BindView(R.id.btn_unload_animal)
    Button btnUnloadAnimal;
    @BindView(R.id.rl_unload_data)
    RelativeLayout rlUnloadData;
    @BindView(R.id.tv_contact)
    TextView tvContact;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_phone)
    TextView tvPhone;
    @BindView(R.id.property_notes)
    TextView tvPropertyNotesTitle;
    @BindView(R.id.rl_location_info)
    RelativeLayout rlLocationInfo;
    private int flag;
    private double latitude = 0.0, longitude;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private ParseObject goCenterObject;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.location_info));
        getLayoutInflater().inflate(R.layout.activity_location_info, contentFrameLayout);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnLoadAnimal.setOnClickListener(this);
        btnUnloadAnimal.setOnClickListener(this);
        progressDialogUtil.showDialog();
        if (getIntent().hasExtra(Constant.LOAD_FLAG)) {
            flag = getIntent().getIntExtra(Constant.LOAD_FLAG, 2);
            if (flag == 1) {
                titleName.setText(getString(R.string.center_info));
                btnLoadAnimal.setVisibility(View.GONE);
                rlLocationInfo.setVisibility(View.GONE);
                rlUnloadData.setVisibility(View.VISIBLE);
                btnUnloadAnimal.setVisibility(View.VISIBLE);
            } else if (flag == 0) {
                titleName.setText(getString(R.string.location_info));
                btnLoadAnimal.setVisibility(View.VISIBLE);
                rlLocationInfo.setVisibility(View.VISIBLE);
                rlUnloadData.setVisibility(View.GONE);
                btnUnloadAnimal.setVisibility(View.GONE);
                setDataLocationInfo(getIntent().getStringExtra(Constant.FOUND_LOCATION));
            }

        }

    }

    @Override
    public void onImageSuccess(Intent data) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load_animal:
                if (getIntent().hasExtra(Constant.FOUND_LOCATION)) {
                    Intent intent = new Intent(this, LoadAnimalActivity.class);
                    intent.putExtra(Constant.FOUND_LOCATION, getIntent().getStringExtra(Constant.FOUND_LOCATION));
                    intent.putExtra(Constant.FOUND_LAT, getIntent().getStringExtra(Constant.FOUND_LAT));
                    intent.putExtra(Constant.FOUND_LONG, getIntent().getStringExtra(Constant.FOUND_LONG));
                    if (!TextUtils.isEmpty(tvName.getText().toString())) {
                        intent.putExtra(Constant.EVT_CENTER_NAME, tvName.getText().toString());
                    }
                    startActivity(intent);
                }
                break;
            case R.id.btn_unload_animal:
                Intent intent = new Intent(LocationInfoActivity.this, UnloadAnimalActivity.class);
                intent.putExtra(Constant.GO_CENTER, goCenterObject);
                if (!TextUtils.isEmpty(tvName.getText().toString())) {
                    intent.putExtra(Constant.EVT_CENTER_NAME, tvName.getText().toString());
                }
                startActivity(intent);
                break;
        }
    }

    private void setDataCenterLocation(String found_location) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("EvacCenter");
        //query.include("UserProperty");
        query.getInBackground(found_location, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {

                if (e == null) {
                    progressDialogUtil.dismissDialog();
                    goCenterObject = object;
                    tvAddress.setText(object.getString("Address"));
                    tvPhone.setText(object.getString("Phone"));
                    tvContact.setText(object.getString("Contact"));
                    tvPropertyNotes.setText(object.getString("CenterNotes"));
                    tvState.setText(object.getString("State"));
                    tvZipcode.setText(object.getString("ZipCode"));
                    tvCity.setText(object.getString("City"));
                    tvName.setText(object.getString("Name"));
                    Log.e("center id = ", object.getObjectId());
                    tvPropertyNotesTitle.setText(getString(R.string.center_note));
                } else {
                    progressDialogUtil.dismissDialog();
                    Toast.makeText(LocationInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setDataLocationInfo(String found_location) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Evacuations");
        query.include("UserProperty");
        query.include("UserLocation");
        query.getInBackground(found_location, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {

                if (e == null) {
                    progressDialogUtil.dismissDialog();
                    ParseUser newUser = object.getParseUser("UserProperty");
                    if (newUser == null) {
                        return;
                    }
                    try {
                        Log.e(TAG, "location info = " + new BaseUtility().parseObjectToJson(object) + "");
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    tvAddress.setText(newUser.getString("Address"));
                    tvFirstName.setText(newUser.getString("FirstName"));
                    tvLastName.setText(newUser.getString("LastName"));
                    tvPropertyNotes.setText(newUser.getString("PropertyNotes"));
                    tvState.setText(newUser.getString("State"));
                    tvZipcode.setText(newUser.getString("ZipCode"));
                    tvCity.setText(newUser.getString("City"));
                    tvEmail.setText(newUser.getEmail());
                    tvMobile.setText(newUser.getString("MobileNumber"));

                } else {
                    progressDialogUtil.dismissDialog();
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(LocationInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendLocation() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", parseUser.getObjectId());
            object.putOpt("session_id", parseUser.getSessionToken());
            object.putOpt("lat", latitude);
            object.putOpt("lon", longitude);
            Log.e(TAG, object + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post("https://evacu.pet/alert-now/get-center.php")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "Api response = " + response.toString());
                        String found_location = response.optString("found_location");
                        if (!TextUtils.isEmpty(found_location)) {
                            if (flag == 1) {
                                setDataCenterLocation(found_location);
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, anError.getMessage());
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (new LocationOnUtility(this, this).enableLocation()) {
            if (latitude == 0.0) {
                trackLocation();
            } else {
                if (mFusedLocationProviderClient != null && mLocationRequest != null) {
                    mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
            }
        }
    }

    @SuppressLint({"MissingPermission"})
    private void trackLocation() {
        if (latitude == 0.0) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<Location> locationList = locationResult.getLocations();
                    if (locationList.size() > 0) {
                        //The last location in the list is the newest
                        Location location = locationList.get(locationList.size() - 1);
                        Log.e("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    }
                }
            };

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(120000
            );
            mLocationRequest.setFastestInterval(120000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(Task<Location> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            Location newLocation = task.getResult();
                            latitude = newLocation.getLatitude();
                            longitude = newLocation.getLongitude();
                            sendLocation();
                            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                        } else {
                            trackLocation();
                        }
                    }
                }
            });
        }
    }
}
