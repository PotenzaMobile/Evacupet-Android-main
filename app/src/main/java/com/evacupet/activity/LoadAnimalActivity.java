package com.evacupet.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.adapter.LoadAnimalListAdapter;
import com.evacupet.interfaceHelper.UpdateAnimalImageClick;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.CompressImageUtility;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

public class LoadAnimalActivity extends DashboardActivity implements View.OnClickListener, UpdateAnimalImageClick {
    private final int REQUEST_CODE_CLICK_IMAGE = 102;
    private final int REQUEST_ACCESS_MEDIA_PERMISSION = 104;
    private static final int ZXING_CAMERA_PERMISSION = 102;
    private static final String TAG = LoadAnimalActivity.class.getSimpleName();
    @BindView(R.id.btn_load_animal)
    Button btnLoadAnimal;
    @BindView(R.id.rv_load_animal)
    RecyclerView rvLoadAnimal;
    private String selectedImagePath;
    private LoadAnimalListAdapter animalListAdapter;
    private ImageView animalImage;
    private List<ParseObject> animalList;
    private String foundLocation;
    private int imagePosition;
    private double lat, lnge;
    private LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location bestEffortAtLocation;
    private ProgressDialogUtil progressDialogUtil;
    private boolean checkDialog = false;
    private String evtCenterName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.load_animals));
        getLayoutInflater().inflate(R.layout.activity_load_animal, contentFrameLayout);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnLoadAnimal.setOnClickListener(this);
        if (getIntent().hasExtra(Constant.FOUND_LOCATION)) {
            foundLocation = getIntent().getStringExtra(Constant.FOUND_LOCATION);
            lat = getIntent().getDoubleExtra(Constant.FOUND_LAT, 0);
            lnge = getIntent().getDoubleExtra(Constant.FOUND_LONG, 0);
            if (getIntent().hasExtra(Constant.EVT_CENTER_NAME)) {
                evtCenterName = getIntent().getStringExtra(Constant.EVT_CENTER_NAME);
            }
            progressDialogUtil.showDialog();
            setAdapter();
            setLoadAnimalData();
        }
    }

    @Override
    public void onImageSuccess(Intent data) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load_animal:
                checkImage();
                break;
        }
    }

    private void checkImage() {
        boolean isImage = false;
        for (final ParseObject object : animalList) {
            if (object.getInt("Status") != 2) {
                isImage = true;
            }
        }
        if (isImage) {
            Toast.makeText(this, "Please capture animal image", Toast.LENGTH_SHORT).show();
        } else {
            doneLoadAnimal();
        }


    }

    private void setLoadAnimalData() {
        animalList = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<>("Evacuations");
        query.include("UserProperty");
        query.getInBackground(foundLocation, new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject object, ParseException e) {

                if (e == null) {
                    //  ParseUser newUser = object.getParseUser("UserProperty");
                    ParseObject newLoc = object.getParseObject("UserLocation");
                    ParseQuery<ParseObject> animalQuery = new ParseQuery<ParseObject>("Animals");
                    animalQuery.whereEqualTo("HomeLocation", newLoc);
                    animalQuery.whereEqualTo("Status", 4);
                    animalQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            progressDialogUtil.dismissDialog();
                            if (e == null) {
                                for (ParseObject object1 : objects) {
                                    try {
                                        Log.e("load animal data = ", new BaseUtility().parseObjectToJson(object1) + "");
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                animalList = objects;
                                animalListAdapter.setData(objects);
                                animalListAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("animal error = ", e.getMessage());
                            }
                        }
                    });

                } else {
                    progressDialogUtil.dismissDialog();
                    Log.e(" error = ", e.getMessage());
                }
            }
        });
    }

    private void setAdapter() {
        animalListAdapter = new LoadAnimalListAdapter(this, this);
        rvLoadAnimal.setLayoutManager(new LinearLayoutManager(LoadAnimalActivity.this));
        rvLoadAnimal.setAdapter(animalListAdapter);
    }

    @Override
    public void itemClick(ParseObject data, int position, ImageView imageView) {
        animalImage = imageView;
        imagePosition = position;
        checkAndRequestPermissions();
    }

    private void doneLoadAnimal() {
        if (animalList != null && animalList.size() > 0) {
            for (final ParseObject object : animalList) {
                if (object.getInt("Status") == 2) {
                    progressDialogUtil.showDialog();
                    object.put("EvacuatedBy", ParseUser.getCurrentUser());
                    object.put("Status", 2);
                    try {
                        if (object.getParseFile("PickupPic") != null) {
                            object.put("PickupPic", object.getParseFile("PickupPic"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseUser parseUser = ParseUser.getCurrentUser();
                                int caps = Integer.parseInt(parseUser.getString("Capacity"));
                                int capsU = parseUser.getInt("CapacityUsed");
                                int left = caps - capsU;
                                if (left < 1) {
                                    trackLocation();
                                    return;
                                }
                                String trailer = parseUser.getString("TrailerType");
                                String capacity = parseUser.getString("Capacity");
                                volunteerApi(capacity, trailer);
                                new BaseUtility().sendNotification(object.getParseObject("Owner").getObjectId(), "Your animals have been picked up and are enroute to the following evacuation center:" + evtCenterName);

                            } else {
                                progressDialogUtil.dismissDialog();
                                Log.e("error == ", e.getMessage());
                                object.saveEventually();
                            }
                        }
                    });

                }
            }
        }
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
                        Location newLocation = task.getResult();
                        if (newLocation.getAccuracy() < 0) {
                            return;
                        }
                        if (bestEffortAtLocation == null || bestEffortAtLocation.getAccuracy() > newLocation.getAccuracy()) {
                            bestEffortAtLocation = newLocation;
                            sendLocation();
                            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                        }

                    } else {
                        trackLocation();
                    }
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
            object.putOpt("lat", bestEffortAtLocation.getLatitude());
            object.putOpt("lon", bestEffortAtLocation.getLongitude());
          /*  object.putOpt("lat", 34.16869);
            object.putOpt("lon", -118.57029);*/
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
                        Log.e(TAG, "sendLocation =" + response.toString());
                        String found_location = response.optString("found_location");
                        if (!TextUtils.isEmpty(found_location)) {
                            double foundLatitude = response.optDouble("Found_Lat");
                            double foundLongitude = response.optDouble("Found_Lon");
                            Intent intent = new Intent(LoadAnimalActivity.this, EvacuationProgressActivity.class);
                            intent.putExtra(Constant.FOUND_LAT, foundLatitude);
                            intent.putExtra(Constant.FOUND_LONG, foundLongitude);
                            intent.putExtra(Constant.FOUND_LOCATION, found_location);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(LoadAnimalActivity.this, FindEvacationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, anError.getMessage());
                    }
                });

    }

    private void volunteerApi(String cap, String trailer) {
        ParseUser parseUser = ParseUser.getCurrentUser();
        JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", parseUser.getObjectId());
            object.putOpt("session_id", parseUser.getSessionToken());
            object.putOpt("lat", lat);
            object.putOpt("lon", lnge);
            object.putOpt("trailer_type", trailer);
            object.putOpt("capacity", cap);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post("https://evacu.pet/alert-now/volunteer.php")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "volunteerApi = " + response.toString());

                        if (response.has("found_location")) {
                            String found_location = response.optString("found_location");
                            ParseGeoPoint geoPoint = new ParseGeoPoint();
                         /*   geoPoint.setLatitude(response.optDouble("Found_Lat"));
                            geoPoint.setLongitude(response.optDouble("Found_Lon"));*/
                            double foundLatitude = response.optDouble("Found_Lat");
                            double foundLongitude = response.optDouble("Found_Lon");
                            if (!checkDialog) {
                                loadAlert(foundLatitude, foundLongitude, found_location);
                            }
                        } else {
                            trackLocation();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, anError.getMessage());

                    }
                });
    }

    private void openCameraIntent() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(getExternalCacheDir(), "cropped.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, getString(R.string.file_provider_authority), file));
            startActivityForResult(intent, REQUEST_CODE_CLICK_IMAGE);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask), REQUEST_ACCESS_MEDIA_PERMISSION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    public void loadAlert(final double lat, final double lng, final String found_location) {
        checkDialog = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Another Evacuation?");
        builder.setMessage("Do you wish to pick up another animal before heading to center?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent(LoadAnimalActivity.this, EvacuationProgressActivity.class);
                        intent.putExtra(Constant.FOUND_LAT, lat);
                        intent.putExtra(Constant.FOUND_LONG, lng);
                        intent.putExtra(Constant.FOUND_LOCATION, found_location);
                        intent.putExtra(Constant.PIC_ANIMAL, Constant.PIC_ANIMAL);
                        startActivity(intent);
                        finish();

                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        trackLocation();
                    }
                });

        AlertDialog alert1 = builder.create();
        alert1.setCancelable(false);
        alert1.setCanceledOnTouchOutside(false);
        alert1.show();

    }

    public Uri checkAndRequestPermissions() {
        Uri outputImage = null;
        int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), ZXING_CAMERA_PERMISSION);
            return outputImage;
        } else {
            openCameraIntent();
        }
        return outputImage;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CLICK_IMAGE && resultCode == RESULT_OK) {

            Uri selectedImageUri = Uri.fromFile(new File(getExternalCacheDir(), "cropped.jpg"));
            selectedImagePath = new CompressImageUtility().compressImage(this, selectedImageUri.getPath());
            Log.e(TAG, selectedImagePath + "...");
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
            setPickupImage();
            animalImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        openCameraIntent();

                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            new BaseUtility().showDialogOK(LoadAnimalActivity.this, "Camera Permission required for this action",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }
        }
    }

    private void setPickupImage() {
        if (selectedImagePath != null) {
            Object imageObject = null;
            try {
                imageObject = new BaseUtility().readInFile(selectedImagePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Create the ParseFile
            ParseFile file = new ParseFile("Image.png", (byte[]) imageObject);
            // Upload the image into Parse Cloud
            file.saveInBackground();
            try {
                file.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            animalList.get(imagePosition).put("PickupPic", file);
            animalList.get(imagePosition).put("Status", 2);
        }
    }
}
