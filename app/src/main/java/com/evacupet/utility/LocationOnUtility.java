package com.evacupet.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.evacupet.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class LocationOnUtility implements EasyPermissions.PermissionCallbacks{

    protected static final String TAG = "LocationOnOff";
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;
    private Context context;
    private Activity mActivity;
    private static final int RC_LOCATION = 9001;

    public LocationOnUtility(Context context, Activity mActivity) {
        this.context = context;
        this.mActivity = mActivity;
    }

    @SuppressLint("MissingPermission")
    public boolean enableLocation() {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
           return enableGPS();
        } else {
            EasyPermissions.requestPermissions(mActivity, context.getString(R.string.location_rationale), RC_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            return false;
        }

    }

    private boolean enableGPS(){
          final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
          if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(context)) {
              Log.e("isProviderEnabled = ","enabled");
              return true;
          }
          if(!hasGPSDevice(context)){
              Log.e("hasGPSDevice = ","enabled");
              return false;

          }
          if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(context)) {
              Log.e("test","Gps already enabled");
              enableLoc();
              return false;
          }else{
              Log.e("else","Gps already enabled");
              return true;

          }
      }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }


    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
           locationRequest.setInterval(30 * 1000);
           locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

          //  builder.setAlwaysShow(true);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(mActivity, REQUEST_LOCATION);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;

                    }
                }
            });
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_LOCATION:
                enableLocation();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strings, int[] ints) {

    }


}
