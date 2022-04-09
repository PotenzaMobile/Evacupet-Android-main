package com.evacupet.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.evacupet.R;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.Constant;
import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
//import com.here.android.mpa.mapping.SupportMapFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;


import java.lang.ref.WeakReference;

import java.util.List;

import butterknife.ButterKnife;

public class TrackAnimalsActivity extends DashboardActivity {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    private Map map = null;
    //private SupportMapFragment mapFragment = null;
    private AndroidXMapFragment mapFragment = null;
    private PositioningManager posManager;
    private ParseGeoPoint geoPoint = null;
    private Handler mHandler;
    private Runnable runnable;
    private String objectID;
    private ClusterLayer cl;
    private MapMarker mm;

    // map embedded in the map fragment
    PositioningManager.OnPositionChangedListener positionListener = new
            PositioningManager.OnPositionChangedListener() {

                public void onPositionUpdated(PositioningManager.LocationMethod method,
                                              GeoPosition position, boolean isMapMatched) {
                    //   myLocation = position.getCoordinate();
                }

                public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                                 PositioningManager.LocationStatus status) {
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.find_animal));
        getLayoutInflater().inflate(R.layout.activity_find_animals, contentFrameLayout);
        ButterKnife.bind(this);
        //ButterKnife.bind(this, contentFrameLayout.getRootView());
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

    @Override
    public void onImageSuccess(Intent data) {
    }

    private void setupMapFragmentView() {
        if (getIntent().hasExtra(Constant.TRACk_ANIMAL)) {
            ParseObject object = getIntent().getParcelableExtra(Constant.TRACk_ANIMAL);
            objectID = object.getObjectId();
            geoPoint = object.getParseGeoPoint("GPS");
        } else if (getIntent().hasExtra(Constant.GEO_LOCATION)) {
            geoPoint = getIntent().getParcelableExtra(Constant.GEO_LOCATION);
        }
        // Search for the map fragment to finish setup by calling init().
        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Set the map center to the Vancouver region (no animation)
                    if (geoPoint != null) {
                        map.setCenter(new GeoCoordinate(geoPoint.getLatitude(), geoPoint.getLongitude()),
                                Map.Animation.NONE);
                        posManager = PositioningManager.getInstance();
                        map.getPositionIndicator().setVisible(true);
                        // mapLocation = new GeoCoordinate(geoPoint.getLatitude(), geoPoint.getLongitude());
                        mm = new MapMarker();
                        mm.setCoordinate(new GeoCoordinate(geoPoint.getLatitude(), geoPoint.getLongitude()));
                        cl = new ClusterLayer();
                        cl.addMarker(mm);
                        map.addClusterLayer(cl);
                        posManager.addListener(new WeakReference<>(positionListener));
                        posManager.start(
                                PositioningManager.LocationMethod.GPS_NETWORK);
                        refreshLocation();

                    }
                } else {
                    Log.e("ERROR", error.getDetails());
                }
            }
        });
    }

    private void refreshLocation() {
        mHandler = new Handler();
        final int delay = 20000; //milliseconds
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!BaseUtility.isAppIsInBackground(TrackAnimalsActivity.this)) {
                    try {
                        getAllAnimals();
                    } catch (Exception ex) {
                        Log.v("Error", ex.getMessage());
                    }
                    mHandler.postDelayed(this, delay);
                }
            }
        };
        mHandler.post(runnable);
    }

    private void getAllAnimals() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Animals");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        query.whereEqualTo("objectId", objectID);
        query.include("EvacuatedTo");
        query.include("EvacuatedBy");
        query.include("HomeLocation");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object : objects) {
                            try {
                                Log.e("ParseObject= ",new BaseUtility().parseObjectToJson(object)+"");
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            if (object.has("Status")) {
                                if (object.getInt("Status") == 2) {
                                    if (object.has("EvacuatedBy")) {
                                        ParseObject jsonObject = object.getParseUser("EvacuatedBy");
                                        if (jsonObject != null) {
                                            ParseGeoPoint lastLocation = jsonObject.getParseGeoPoint("LastLocation");
                                            if (lastLocation != null) {
                                                cl.removeMarker(mm);
                                                mm.setCoordinate(new GeoCoordinate(lastLocation.getLatitude(), lastLocation.getLongitude()));
                                                cl.addMarker(mm);
                                                map.addClusterLayer(cl);
                                            }
                                        }
                                    }
                                    break;
                                } else if (object.getInt("Status") == 3) {
                                    ParseObject jsonObject = object.getParseUser("EvacuatedTo");
                                    if (jsonObject != null) {
                                        ParseGeoPoint lastLocation = jsonObject.getParseGeoPoint("LocationGPS");
                                        if (lastLocation != null) {
                                            cl.removeMarker(mm);
                                            mm.setCoordinate(new GeoCoordinate(lastLocation.getLatitude(), lastLocation.getLongitude()));
                                            cl.addMarker(mm);
                                            map.addClusterLayer(cl);
                                        }
                                    }
                                    break;
                                }
                            }

                        }
                    }

                } else {
                    Log.e("error = ", e.getMessage());
                }
            }
        });
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
                //setupMapFragmentView();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        mHandler.removeCallbacks(runnable);
    }
}