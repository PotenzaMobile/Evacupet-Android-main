package com.evacupet.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.evacupet.R;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
//import com.here.android.mpa.mapping.SupportMapFragment;
import com.parse.FindCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.List;

import butterknife.ButterKnife;

public class AnimalLocationActivity extends DashboardActivity {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    private Map map = null;
    private PositioningManager posManager;
    // map fragment embedded in this activity
    //private SupportMapFragment mapFragment = null;
    private AndroidXMapFragment mapFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.location));
        getLayoutInflater().inflate(R.layout.activity_animal_location, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        if (hasPermissions(this, RUNTIME_PERMISSIONS)) {
            setupMapFragmentView();
        } else {
            ActivityCompat
                    .requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        }


    }

    private void getAllAnimals() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Animals");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        query.include("EvacuatedTo");
        query.include("EvacuatedBy");
        query.include("HomeLocation");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object1 : objects) {
                            if (object1.getParseGeoPoint("GPS") != null) {
                                ParseGeoPoint parseGeoPoint = object1.getParseGeoPoint("GPS");
                                MapMarker defaultMarker = new MapMarker();
                                defaultMarker.setCoordinate(new GeoCoordinate(parseGeoPoint.getLatitude(), parseGeoPoint.getLatitude(), 0.0));
                                if (!TextUtils.isEmpty(object1.getString("Name"))) {
                                    defaultMarker.setTitle(object1.getString("Name"));
                                }
                                map.addMapObject(defaultMarker);

                            }
                        }
                    }
                } else {
                    Log.e("error = ", e.getMessage());
                }
            }
        });
    }

    private void setupMapFragmentView() {



        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    getAllAnimals();
                } else {
                    Log.e("ERROR", error.getDetails());
                }
            }
        });

    }


    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /*
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat
                                .shouldShowRequestPermissionRationale(this, permissions[index])) {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                            + " not granted. "
                                            + "Please go to settings and turn on for app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                    + " not granted", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                setupMapFragmentView();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (posManager != null) {
            posManager.start(
                    PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (posManager != null) {
            posManager.stop();
        }
    }

    @Override
    public void onImageSuccess(Intent data) {

    }
}
