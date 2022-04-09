package com.evacupet.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.evacupet.R;

import com.evacupet.utility.Constant;
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
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;

import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ViewLocationActivity extends DashboardActivity implements View.OnClickListener {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    @BindView(R.id.tv_current_location)
    TextView tvCurrentLocation;
    @BindView(R.id.tv_start_turn)
    TextView tvStartTurn;
    // map embedded in the map fragment
    private Map map = null;
    private PositioningManager posManager;
    // map fragment embedded in this activity
    //private SupportMapFragment mapFragment = null;
    private AndroidXMapFragment mapFragment = null;
    private GeoCoordinate myLocation, mapLocation;
    private GeoBoundingBox m_geoBoundingBox;
    private ParseGeoPoint geoPoint = null;


    PositioningManager.OnPositionChangedListener positionListener = new
            PositioningManager.OnPositionChangedListener() {

                public void onPositionUpdated(PositioningManager.LocationMethod method,
                                              GeoPosition position, boolean isMapMatched) {
                    myLocation = position.getCoordinate();


                }

                public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                                 PositioningManager.LocationStatus status) {
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_view_location);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.view_location));
        getLayoutInflater().inflate(R.layout.activity_view_location, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        tvCurrentLocation.setOnClickListener(this);
        tvStartTurn.setOnClickListener(this);
        if (hasPermissions(this, RUNTIME_PERMISSIONS)) {
            setupMapFragmentView();
        } else {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        }


    }

    private void setupMapFragmentView() {

        if (getIntent().hasExtra(Constant.VIEW_LOCATION)) {
            ParseObject object = getIntent().getParcelableExtra(Constant.VIEW_LOCATION);
             geoPoint = object.getParseGeoPoint("LocationGPS");
        } else if (getIntent().hasExtra(Constant.GEO_LOCATION)) {
             geoPoint = getIntent().getParcelableExtra(Constant.GEO_LOCATION);
        }
        // Search for the map fragment to finish setup by calling init().
        //mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {



                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Set the map center to the Vancouver region (no animation)
                    if (geoPoint != null) {
                        map.setCenter(new GeoCoordinate(geoPoint.getLatitude(), geoPoint.getLongitude()),Map.Animation.NONE);
                        posManager = PositioningManager.getInstance();
                        map.getPositionIndicator().setVisible(true);
                        mapLocation = new GeoCoordinate(geoPoint.getLatitude(), geoPoint.getLongitude());

                        try {

                            posManager.addListener(new WeakReference<>(positionListener));
                            posManager.start(PositioningManager.LocationMethod.GPS_NETWORK);

                        } catch (Exception ex) {
                            Log.v("Error", ex.getMessage());
                        }

                    }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
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
    public void onImageSuccess(Intent data) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_current_location:
                tvStartTurn.setVisibility(View.VISIBLE);
                tvCurrentLocation.setVisibility(View.GONE);
                setCurrentLocationPath();
                break;
            case R.id.tv_start_turn:
                startTurn();
                break;
        }
    }

    private void setCurrentLocationPath() {
        if (mapLocation != null && myLocation != null) {
            CoreRouter router = new CoreRouter();
            RoutePlan routePlan = new RoutePlan();

            RouteOptions routeOptions = new RouteOptions();
            routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
            routeOptions.setRouteType(RouteOptions.Type.FASTEST);

            routePlan.setRouteOptions(routeOptions);
            routePlan.addWaypoint(new RouteWaypoint(mapLocation));
            routePlan.addWaypoint(new RouteWaypoint(myLocation));
            router.calculateRoute(routePlan, new RouteListener());
        }

    }

    private void startTurn() {
        ParseObject object = getIntent().getParcelableExtra(Constant.VIEW_LOCATION);
        ParseGeoPoint geoPoint = object.getParseGeoPoint("LocationGPS");
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setRealisticViewMode(NavigationManager.RealisticViewMode.NIGHT);
        map.setMapScheme(Map.Scheme.CARNAV_DAY);
        if (mapLocation != null){
            map.setCenter(new GeoCoordinate(geoPoint.getLatitude(), geoPoint.getLongitude()),
                    Map.Animation.NONE);
            if (m_geoBoundingBox != null) {
                map.zoomTo(m_geoBoundingBox, Map.Animation.NONE,
                        Map.MOVE_PRESERVE_ORIENTATION);
            }
        }
      /*  PointF mPointF = new PointF();
        mPointF.set(map.getWidth() / 2, map.getHeight());
        map.setTransformCenter(mPointF);*/


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


    private class RouteListener implements CoreRouter.Listener {

        // Method defined in Listener
        public void onProgress(int percentage) {
            // Display a message indicating calculation progress
        }

        // Method defined in Listener
        public void onCalculateRouteFinished(List<RouteResult> routeResult, RoutingError error) {
            // If the route was calculated successfully
            if (error == RoutingError.NONE) {
                // Render the route on the map
                MapRoute mapRoute = new MapRoute(routeResult.get(0).getRoute());
                m_geoBoundingBox = routeResult.get(0).getRoute().getBoundingBox();
                map.addMapObject(mapRoute);
            } else {
                // Display a message indicating route calculation failure
                Log.e("error = ", error.toString());
            }
        }
    }
}
