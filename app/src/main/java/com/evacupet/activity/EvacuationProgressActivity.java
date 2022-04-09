package com.evacupet.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.utility.Constant;
import com.evacupet.utility.LocationOnUtility;
import com.evacupet.utility.PassData;
import com.evacupet.utility.ProgressDialogUtil;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapRoute;
//import com.here.android.mpa.mapping.SupportMapFragment;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.here.android.mpa.mapping.MapRoute.RenderType.PRIMARY;

public class EvacuationProgressActivity extends DashboardActivity implements View.OnClickListener, PositioningManager.OnPositionChangedListener {
    private static final String TAG = EvacuationProgressActivity.class.getSimpleName();
    @BindView(R.id.ll_search)
    LinearLayout llSearch;
    @BindView(R.id.ll_no_data_found)
    LinearLayout llNoDataFound;
    @BindView(R.id.rl_map)
    RelativeLayout rlMap;
    @BindView(R.id.rl_button)
    RelativeLayout rlButton;
    @BindView(R.id.btn_yes)
    Button btnYes;
    @BindView(R.id.btn_no)
    Button btnNo;
    @BindView(R.id.btn_try_again)
    Button btnTryAgain;
    @BindView(R.id.btn_view_location_info)
    Button btnViewLocationInfo;
    @BindView(R.id.donut_progress)
    DonutProgress donut_progress;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    LocationRequest mLocationRequest;
    private double latitude = 0.0, longitude;
    private double foundLatitude, foundLongitude;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // map embedded in the map fragment
    private Map map = null;
    LocationCallback mLocationCallback;
    // map fragment embedded in this activity
    //private SupportMapFragment mapFragment = null;
    private AndroidXMapFragment mapFragment = null;
    private ProgressDialogUtil progressDialogUtil;
    private String foundLocation;
    private PositioningManager posManager;
    private GeoCoordinate myLocation, mapLocation;
    private GeoBoundingBox geoBoundingBox;
    private MapRoute mapRoute;
    private int loadFlag = 0;
    private boolean checkLoadMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.evacuation_center));
        getLayoutInflater().inflate(R.layout.activity_evacuation_progress, contentFrameLayout);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);
        btnTryAgain.setOnClickListener(this);
        btnTryAgain.setVisibility(View.INVISIBLE);
        btnViewLocationInfo.setOnClickListener(this);
    }


    @Override
    public void onImageSuccess(Intent data) {

    }

    private void setupMapFragmentView() {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    checkLoadMap = true;
                    posManager = PositioningManager.getInstance();
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)

                    map.setCenter(new GeoCoordinate(foundLatitude, foundLongitude),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel(13.2);
                    mapLocation = new GeoCoordinate(foundLatitude, foundLongitude);
                    myLocation = new GeoCoordinate(latitude, longitude);
                    //  myLocation = new GeoCoordinate(34.16869, -118.57029);
                    try {
                        // posManager.addListener(new WeakReference<>(this));
                        posManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(
                                EvacuationProgressActivity.this));
                        posManager.start(
                                PositioningManager.LocationMethod.GPS_NETWORK_INDOOR);

                    } catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }

                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
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
                //setupMapFragmentView();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                rlButton.setVisibility(View.GONE);
                acceptEvacuation();
                break;
            case R.id.btn_no:
                PassData.status = false;
                Intent intent = new Intent(EvacuationProgressActivity.this, FindEvacationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;

            case R.id.btn_view_location_info:
                PassData.status = false;
                Intent intent1 = new Intent(EvacuationProgressActivity.this, LocationInfoActivity.class);
                intent1.putExtra(Constant.FOUND_LOCATION, foundLocation);
                intent1.putExtra(Constant.FOUND_LAT, foundLatitude);
                intent1.putExtra(Constant.FOUND_LONG, foundLongitude);
                intent1.putExtra(Constant.LOAD_FLAG, loadFlag);
                startActivity(intent1);
                finish();
                break;
            case R.id.btn_try_again:
                llSearch.setVisibility(View.VISIBLE);
                llNoDataFound.setVisibility(View.GONE);
                rlMap.setVisibility(View.GONE);
                locationSearching();
                break;
        }
    }

    private void locationSearching() {
        ParseUser user = ParseUser.getCurrentUser();
        progressDialogUtil.showDialog();
        JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", user.getObjectId());
            object.putOpt("session_id", user.getSessionToken());
            object.putOpt("trailer_type", user.getString("TrailerType"));
            object.putOpt("capacity", user.getString("Capacity"));
            object.putOpt("lat", String.valueOf(latitude));
            object.putOpt("lon", String.valueOf(longitude));

          /*  object.putOpt("lat", String.valueOf(34.16869));
            object.putOpt("lon", String.valueOf(-118.57029));*/
            //Log.e("EEE: ", object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("EEE: ", object.toString());
        AndroidNetworking.post("https://evacu.pet/alert-now/volunteer.php")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "locationSearching = " + response.toString());
                        if (response.optString("status").equals("Pass")) {
                            rlMap.setVisibility(View.VISIBLE);
                            PassData.status = true;
                            llSearch.setVisibility(View.GONE);
                            llNoDataFound.setVisibility(View.GONE);
                            if (hasPermissions(EvacuationProgressActivity.this, RUNTIME_PERMISSIONS)) {
                                foundLatitude = response.optDouble("Found_Lat");
                                foundLongitude = response.optDouble("Found_Lon");
                                setupMapFragmentView();
                                foundLocation = response.optString("found_location");
                            } else {
                                ActivityCompat
                                        .requestPermissions(EvacuationProgressActivity.this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        } else {
                            llSearch.setVisibility(View.GONE);
                            rlMap.setVisibility(View.GONE);
                            llNoDataFound.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                    }
                });
    }

    private void acceptEvacuation() {
        ParseUser user = ParseUser.getCurrentUser();
        JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", user.getObjectId());
            object.putOpt("session_id", user.getSessionToken());
            object.putOpt("target", foundLocation);
            object.putOpt("lat", String.valueOf(latitude));
            object.putOpt("lon", String.valueOf(longitude));
            Log.e(TAG, object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialogUtil.showDialog();
        AndroidNetworking.post("https://evacu.pet/alert-now/driver_accept.php")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        setCurrentLocationPath();
                        PassData.status = true;
                        Log.e(TAG, "driver_accept = " + response.toString());
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, anError.getMessage());
                        progressDialogUtil.dismissDialog();
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
                    Log.e("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
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
                        Location location = task.getResult();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                         Log.e(TAG,"latitude="+latitude+"=longitude="+longitude);
                        if (!getIntent().hasExtra(Constant.FOUND_LOCATION)) {
                            locationSearching();
                        }
                        if (loadFlag == 1){
                            myLocation = new GeoCoordinate(latitude, longitude);
                            setCurrentLocationPath();
                            PassData.status = true;
                        }
                    } else {
                        trackLocation();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!checkLoadMap) {
            if (getIntent().hasExtra(Constant.FOUND_LOCATION)) {
                foundLatitude = getIntent().getDoubleExtra(Constant.FOUND_LAT, 0);
                foundLongitude = getIntent().getDoubleExtra(Constant.FOUND_LONG, 0);
                foundLocation = getIntent().getStringExtra(Constant.FOUND_LOCATION);
                Log.e("bgfgh = ",foundLatitude+"jfgd"+foundLongitude);
                rlMap.setVisibility(View.VISIBLE);
                PassData.status = true;
                llSearch.setVisibility(View.GONE);
                llNoDataFound.setVisibility(View.GONE);
                setupMapFragmentView();
                if (getIntent().hasExtra(Constant.PIC_ANIMAL)) {
                    loadFlag = 0;
                } else {
                    loadFlag = 1;
                    btnViewLocationInfo.setText(getString(R.string.view_to_center));
                    rlButton.setVisibility(View.GONE);
                    //acceptEvacuation();

                }
            } else {
                // locationSearching();
            }
        }

        if (new LocationOnUtility(this, this).enableLocation()) {
            if (latitude == 0.0) {
                trackLocation();
            } else {
                if (mFusedLocationProviderClient != null && mLocationRequest != null) {
                    mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
            }
        }
        if (posManager != null) {
            posManager.start(
                    PositioningManager.LocationMethod.GPS_NETWORK_INDOOR);
        }

    }

    private void setCurrentLocationPath() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, mapLocation + "..." + myLocation);
                if (mapLocation != null && myLocation != null) {
                    Log.e(TAG, "working");
                    CoreRouter router = new CoreRouter();
                    RoutePlan routePlan = new RoutePlan();
                    RouteOptions routeOptions = new RouteOptions();
                    routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                    routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                    routePlan.setRouteOptions(routeOptions);
                    routePlan.addWaypoint(new RouteWaypoint(myLocation));
                    routePlan.addWaypoint(new RouteWaypoint(mapLocation));
                    router.calculateRoute(routePlan, new Router.Listener<List<RouteResult>, RoutingError>() {
                        @Override
                        public void onProgress(int i) {
                            donut_progress.setVisibility(View.VISIBLE);
                            donut_progress.setProgress(i);
                            Log.e(TAG, i + "");
                        }

                        @Override
                        public void onCalculateRouteFinished(List<RouteResult> routeResults, RoutingError routingError) {
                            donut_progress.setVisibility(View.GONE);
                            if (routingError == RoutingError.NONE) {
                                // Render the route on the map

                                mapRoute = new MapRoute(routeResults.get(0).getRoute());
                                Route route = routeResults.get(0).getRoute();
                                map.addMapObject(mapRoute);
                                geoBoundingBox = routeResults.get(0).getRoute().getBoundingBox();
                                map.zoomTo(geoBoundingBox, Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                                EvacuationNavigationAlert(route);
                            } else {
                                // Display a message indicating route calculation failure
                                Toast.makeText(EvacuationProgressActivity.this, routingError.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("error = ", routingError.toString());
                            }
                        }
                    });
                }
            }
        });
    }


    private void EvacuationNavigationAlert(final Route route) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Navigation mode");
        builder.setMessage("Please choose a mode");
        builder.setCancelable(true);
        builder.setNegativeButton(
                "Navigation",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        MapNavigation(route);
                    }
                });

        builder.setPositiveButton(
                "Simulation",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mapSimulation(route);
                    }
                });

        AlertDialog alert1 = builder.create();
        alert1.setCancelable(false);
        alert1.setCanceledOnTouchOutside(false);
        alert1.show();


    }

    private void MapNavigation(Route route) {
        map.getPositionIndicator().setVisible(true);
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.startNavigation(route);
        navigationManager.setSpeedWarningEnabled(true);
        map.setTilt(60);
        map.setZoomLevel(19);
        map.setOrientation(1);
        map.setExtrudedBuildingsVisible(true);
        map.setCenter(new GeoCoordinate(foundLatitude, foundLongitude),
                Map.Animation.NONE);
        map.zoomTo(geoBoundingBox, Map.Animation.NONE,
                Map.MOVE_PRESERVE_ORIENTATION);

    }

    private void mapSimulation(Route route) {
        Log.e("SFsdfsd0", "testing" + route.toString());
        map.getPositionIndicator().setVisible(true);
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setMap(map);
        navigationManager.simulate(route, 60);
        navigationManager.setSpeedWarningEnabled(true);
        map.setTilt(60);
        map.setZoomLevel(19);
        map.setOrientation(1);
        mapRoute.setRenderType(PRIMARY);
        map.setExtrudedBuildingsVisible(true);
        map.setCenter(new GeoCoordinate(foundLatitude, foundLongitude),
                Map.Animation.NONE);
        map.zoomTo(geoBoundingBox, Map.Animation.NONE,
                Map.MOVE_PRESERVE_ORIENTATION);

        navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
        setButtonTimer();

    }

    private void setButtonTimer() {
        new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.e(TAG, "finish = ");
                btnViewLocationInfo.setVisibility(View.VISIBLE);
            }
        }.start();

    }

    @Override
    public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
        myLocation = geoPosition.getCoordinate();
        // myLocation = new GeoCoordinate(34.16869, -118.57029);
    }

    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

    }
}
