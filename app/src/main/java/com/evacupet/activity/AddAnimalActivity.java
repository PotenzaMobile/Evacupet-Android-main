package com.evacupet.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.adapter.AnimalImageAdapter;
import com.evacupet.fragment.LocationFragment;
import com.evacupet.interfaceHelper.AddLocationClick;
import com.evacupet.model.AnimalImageModel;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.CompressImageUtility;
import com.evacupet.utility.ConnectionUtil;
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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;

public class AddAnimalActivity extends DashboardActivity implements View.OnClickListener, AddLocationClick, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = AddAnimalActivity.class.getSimpleName();
    @BindView(R.id.ed_location)
    TextView edLocation;
    @BindView(R.id.tv_species)
    TextView tvSpecies;
    @BindView(R.id.sp_gender)
    Spinner spGender;
    @BindView(R.id.ed_home_location)
    TextView edHomeLocation;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.btn_add_photo)
    Button btnAddPhoto;
    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.ed_bread)
    EditText edBread;
    @BindView(R.id.ed_bday)
    EditText edBday;
    @BindView(R.id.ed_color)
    EditText edColor;
    @BindView(R.id.ed_height)
    EditText edHeight;
    @BindView(R.id.ed_tattoo)
    EditText edTattoo;
    @BindView(R.id.ed_microchip)
    EditText edMicrochip;
    @BindView(R.id.ed_behavior)
    EditText edBehavior;
    @BindView(R.id.ed_dietary)
    EditText edDietary;
    @BindView(R.id.ed_facility)
    EditText edMdical;
    @BindView(R.id.ed_medical)
    EditText edFacility;
    @BindView(R.id.sp_trailer)
    Spinner spTrailer;
    @BindView(R.id.iv_animals_image)
    ImageView ivAnimalsImage;
    @BindView(R.id.fm_location)
    FrameLayout fmLocation;
    @BindView(R.id.fm_home_location)
    FrameLayout fmHomeLocation;
    @BindView(R.id.rl_location)
    RelativeLayout rlLocation;
    @BindView(R.id.rl_home_location)
    RelativeLayout rlHomeLoc;
    @BindView(R.id.rv_animal)
    RecyclerView rvAnimal;
    private String selectedImagePath;
    private Uri outputFileUri;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<AnimalImageModel> imageModelArrayList;
    private double latitude = 0.0, longitude;
    private ParseObject locationObject, homeLocationObject;
    private String trailer, species, gender;
    private ParseGeoPoint gps, homeGeoPoint;
    private ProgressDialogUtil progressDialogUtil;
    private boolean isResume = false;
    private LocationCallback mLocationCallback;
    private AnimalImageAdapter imageAdapter;
    private ParseObject newAnimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_add_animal, contentFrameLayout);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        imageModelArrayList = new ArrayList<>();
        progressDialogUtil = new ProgressDialogUtil(this);
        edHomeLocation.setOnClickListener(this);
        edLocation.setOnClickListener(this);
        edBday.setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        tvSpecies.setOnClickListener(this);
        setImageAdapter();
        setTrailerAdapter();
        setAnimalData();

        edName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edName.clearFocus();
                    tvSpecies.requestFocus();
                    onClick(tvSpecies);
                    return true;
                }
                return false;
            }
        });

        edBread.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edBread.clearFocus();
                    edBday.requestFocus();
                    onClick(edBday);
                    return true;
                }
                return false;
            }
        });

        edBday.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edBday.clearFocus();
                    edColor.requestFocus();
                    onClick(edColor);
                    return true;
                }
                return false;
            }
        });

        edColor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edColor.clearFocus();
                    edHeight.requestFocus();
                    onClick(edHeight);
                    return true;
                }
                return false;
            }
        });

        edHeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edHeight.clearFocus();
                    edTattoo.requestFocus();
                    onClick(edTattoo);
                    return true;
                }
                return false;
            }
        });

        edTattoo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edTattoo.clearFocus();
                    edMicrochip.requestFocus();
                    onClick(edMicrochip);
                    return true;
                }
                return false;
            }
        });

        edMicrochip.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //do your stuff here...
                    hideKeyboard(AddAnimalActivity.this);
                    edMicrochip.clearFocus();
                    spTrailer.requestFocus();
                    spTrailer.performClick();
                    return true;
                }
                return false;
            }
        });





    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ed_location:
                if (fmLocation.getVisibility() == View.GONE) {
                    fmLocation.setVisibility(View.VISIBLE);
                    LocationFragment locationFragment = new LocationFragment();
                    locationFragment.setFlag(this, rlLocation, 1);

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fm_location, locationFragment);
                    ft.commit();
                } else {
                    fmLocation.setVisibility(View.GONE);
                }
                break;
            case R.id.ed_home_location:
                if (fmHomeLocation.getVisibility() == View.GONE) {
                    fmHomeLocation.setVisibility(View.VISIBLE);
                    LocationFragment locationFragment2 = new LocationFragment();
                    locationFragment2.setFlag(this, rlHomeLoc, 2);
                    FragmentTransaction ftt = getSupportFragmentManager().beginTransaction();
                    ftt.replace(R.id.fm_home_location, locationFragment2);
                    ftt.commit();
                } else {
                    fmHomeLocation.setVisibility(View.GONE);
                }
                break;

            case R.id.ed_bday:
                openDatePicker();
                break;
            case R.id.btn_add_photo:
                if (imageModelArrayList.size() == 5) {
                    Toast.makeText(this, "Only add up to 5 image!!", Toast.LENGTH_SHORT).show();
                } else {
                    outputFileUri = imageInit();
                }
                break;
            case R.id.btn_save:
                if (locationObject == null) {
                    Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(species)) {
                    Toast.makeText(this, "Please select Animal Species", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty( edBread.getText().toString() )) {
                    Toast.makeText(this, "Please select Weight", Toast.LENGTH_SHORT).show();
                }

                else if ( (trailer.equals("Trailer Requirements") && tvSpecies.getText().toString().equals("Horse")) || trailer.equals("Trailer Requirements") && tvSpecies.getText().toString().equals("Cow")   ) {
                    Toast.makeText(this, "Please select Trailer Requirements", Toast.LENGTH_SHORT).show();
                }

                else {
                    if (ConnectionUtil.isInternetOn(this)) {
                        progressDialogUtil.showDialog();
                        addAnimals();
                    } else {
                        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.tv_species:
                PopupMenu popup = new PopupMenu(this, v);
                popup.setOnMenuItemClickListener(this);
                popup.inflate(R.menu.species_menu);
                popup.show();
                break;
        }
    }


    @Override
    public void onImageSuccess(Intent data) {
        final boolean isCamera;
        if ( data == null || data.getDataString() == null ) {
            isCamera = true;
        } else {
            final String action = data.getAction();
            if (action == null) {
                isCamera = false;
            } else {
                isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
            }
        }
        Uri selectedImageUri;
        String path;
        if (isCamera) {
            selectedImageUri = outputFileUri;
            selectedImagePath = new CompressImageUtility().compressImage(this, selectedImageUri.getPath());
            //   selectedImagePath = selectedImageUri.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
            imageModelArrayList.add(new AnimalImageModel(bitmap, selectedImagePath, "", 0));
            imageAdapter.setData(imageModelArrayList, 1);
            imageAdapter.notifyDataSetChanged();

            rvAnimal.setVisibility(View.VISIBLE);
            /*ivAnimalsImage.setVisibility(View.VISIBLE);
            ivAnimalsImage.setImageBitmap(bitmap);*/

        } else {
            selectedImageUri = data == null ? null : data.getData();
            path = new BaseUtility().getPath(this, selectedImageUri);
            //  selectedImagePath = path;
            selectedImagePath = new CompressImageUtility().compressImage(this, path);
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
            imageModelArrayList.add(new AnimalImageModel(bitmap, selectedImagePath, "", 0));
            imageAdapter.setData(imageModelArrayList, 1);
            imageAdapter.notifyDataSetChanged();
            rvAnimal.setVisibility(View.VISIBLE);
         /*   ivAnimalsImage.setVisibility(View.VISIBLE);
            ivAnimalsImage.setImageBitmap(bitmap);*/
        }
    }

    public void openDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d("", "DATE SELECTED " + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        edBday.setText((monthOfYear + 1) + "-" + dayOfMonth + "-" + year);

                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setAnimalData() {
        if (getIntent().hasExtra(Constant.ANIMAL_DATA)) {
            titleName.setText(getString(R.string.edit_animals));
            ParseObject object = getIntent().getParcelableExtra(Constant.ANIMAL_DATA);
            edName.setText(object.getString("Name"));
            if (object.getDate("Birthday") != null) {

                edBday.setText(new BaseUtility().convertDateToString(object.getDate("Birthday")));
            }
            edColor.setText(object.getString("Color"));
            edHeight.setText(object.getString("Height"));
            edBread.setText(object.getString("weight"));
            edFacility.setText(object.getString("FacilityDetails"));
            edMicrochip.setText(object.getString("HalterTag"));
            edTattoo.setText(object.getString("Tattoo"));
            edDietary.setText(object.getString("DietHay"));
            edMdical.setText(object.getString("MedicalRequirements"));

            species = object.getString("Species");
            gender = object.getString("Sex");
            tvSpecies.setText(species);
            edBehavior.setText(object.getString("BehaviorRequirements"));
          //  gps = object.getParseGeoPoint("GPS");

            locationObject = object.getParseObject("HomeLocation");
            edLocation.setText(locationObject.getString("address"));

            if (!TextUtils.isEmpty(gender)) {
                for (int i = 0; i < getResources().getStringArray(R.array.animalGender).length; i++) {
                    String[] genderArray = getResources().getStringArray(R.array.animalGender);
                    String animalGender = genderArray[i];
                    if (gender.equals(animalGender)) {
                        spGender.setSelection(i);

                    }
                }
            }
            if (object.getParseFile("Image") != null) {
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = object.getParseFile("Image").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file, 0, profile_image_file.length);
                imageModelArrayList.add(new AnimalImageModel(profile_image_bitmap, "", object.getObjectId(), 1));
            }
            if (object.getParseFile("ImageOne") != null) {
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = object.getParseFile("ImageOne").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file, 0, profile_image_file.length);
                imageModelArrayList.add(new AnimalImageModel(profile_image_bitmap, "", object.getObjectId(), 2));
            }
            if (object.getParseFile("ImageTwo") != null) {
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = object.getParseFile("ImageTwo").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file, 0, profile_image_file.length);
                imageModelArrayList.add(new AnimalImageModel(profile_image_bitmap, "", object.getObjectId(), 3));
            }
            if (object.getParseFile("ImageThree") != null) {
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = object.getParseFile("ImageThree").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file, 0, profile_image_file.length);
                imageModelArrayList.add(new AnimalImageModel(profile_image_bitmap, "", object.getObjectId(), 4));
            }
            if (object.getParseFile("ImageFour") != null) {
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = object.getParseFile("ImageFour").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file, 0, profile_image_file.length);
                imageModelArrayList.add(new AnimalImageModel(profile_image_bitmap, "", object.getObjectId(), 5));
            }
            if (imageModelArrayList != null && !imageModelArrayList.isEmpty()) {
                imageAdapter.setData(imageModelArrayList, 2);
                imageAdapter.notifyDataSetChanged();
                rvAnimal.setVisibility(View.VISIBLE);
            }

        } else {
            titleName.setText(getString(R.string.ad_animals));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (new LocationOnUtility(this, this).enableLocation()) {
            trackLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void saveCurrentLocation() {
        final ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Locations");
            parseQuery.whereEqualTo("Owner", user);
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects != null && objects.size() > 0) {

                        } else {
                            List<Address> addresses = getAddressByLocation();
                            ParseGeoPoint geo = new ParseGeoPoint(latitude, longitude);
                            ParseObject object = new ParseObject("Locations");
                            if (addresses.size() > 0) {
                                object.put("address", addresses.get(0).getAddressLine(0));
                                object.put("city", addresses.get(0).getLocality());
                                object.put("state", addresses.get(0).getAdminArea());
                                object.put("zip", addresses.get(0).getPostalCode());
                            }
                            object.put("Owner", user);
                            object.put("GPS", geo);
                            object.put("isMain", 1);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.e("sucess = ", "done");
                                        isResume = true;
                                    } else {
                                        Log.e("error = ", e.getMessage());
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(AddAnimalActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @SuppressLint({"MissingPermission"})
    private void trackLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (latitude == 0.0) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<Location> locationList = locationResult.getLocations();
                    if (locationList.size() > 0) {
                        //The last location in the list is the newest
                        Location location = locationList.get(locationList.size() - 1);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        //   mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                        Log.e("MapsActivity add", "Location: " + location.getLatitude() + " " + location.getLongitude());
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
                            if (!getIntent().hasExtra(Constant.ANIMAL_DATA)) {
                                if (!isResume) {
                                    saveCurrentLocation();
                                }
                            }
                            if (mFusedLocationProviderClient != null) {
                                mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                            }
                        } else {
                            trackLocation();

                        }
                    }
                }
            });
        }
    }

    public List<Address> getAddressByLocation() {
        List<Address> addresses = null;
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Log.e("addresses.get(0)", addresses.get(0).getAddressLine(0) + "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    public void itemClick(ParseObject data, int flag) {
        if (flag == 1) {
            locationObject = data;
            //homeLocationObject = data;
            gps = locationObject.getParseGeoPoint("GPS");
            fmLocation.setVisibility(View.GONE);
            edLocation.setText(locationObject.getString("address"));
        } /*else {
            homeLocationObject = data;
            homeGeoPoint = homeLocationObject.getParseGeoPoint("GPS");
            fmHomeLocation.setVisibility(View.GONE);
            edHomeLocation.setText(homeLocationObject.getString("address"));
        }*/
    }

    private void setTrailerAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.trailerReq));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spTrailer.setAdapter(adapter);
        spTrailer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trailer = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.animalGender));
        genderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spGender.setAdapter(genderAdapter);
        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = genderAdapter.getItem(position);
                if (gender.equals("Select gender")) {
                    gender = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void addAnimals() {
        newAnimal = getIntent().getParcelableExtra(Constant.ANIMAL_DATA);
        if (newAnimal == null) {
            newAnimal = new ParseObject("Animals");
        }
        if (!TextUtils.isEmpty(edBday.getText().toString())) {
            newAnimal.put("Birthday", new BaseUtility().convertDate(edBday.getText().toString()));
        }
        newAnimal.put("weight", edBread.getText().toString());
        newAnimal.put("Color", edColor.getText().toString());
        newAnimal.put("DietHay", edDietary.getText().toString());
        newAnimal.put("MedicalRequirements", edMdical.getText().toString());
        newAnimal.put("Species", tvSpecies.getText().toString());
        newAnimal.put("Sex", gender);
        newAnimal.put("FacilityDetails", edFacility.getText().toString());
        if (gps != null) {
            newAnimal.put("GPS", gps);
            newAnimal.put("HomeLocation", locationObject);
        }
        newAnimal.put("HalterTag", edMicrochip.getText().toString());
        newAnimal.put("Height", edHeight.getText().toString());
        newAnimal.put("BehaviorRequirements", edBehavior.getText().toString());
      /*  if (homeGeoPoint != null) {
            newAnimal.put("HomeLocation", homeLocationObject);
        }*/
        newAnimal.put("Name", edName.getText().toString());
        newAnimal.put("Tattoo", edTattoo.getText().toString());
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (imageModelArrayList != null && !imageModelArrayList.isEmpty()) {
                    for (int i = 0; i < imageModelArrayList.size(); i++) {
                        if (!TextUtils.isEmpty(imageModelArrayList.get(i).getImagePath())) {
                            String ImageParam = "";
                            if (i == 0) {
                                ImageParam = "Image";
                            } else if (i == 1) {
                                ImageParam = "ImageOne";
                            } else if (i == 2) {
                                ImageParam = "ImageTwo";
                            } else if (i == 3) {
                                ImageParam = "ImageThree";
                            } else if (i == 4) {
                                ImageParam = "ImageFour";
                            }
                            Object imageObject = null;
                            try {
                                imageObject = new BaseUtility().readInFile(imageModelArrayList.get(i).getImagePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Create the ParseFile
                            ParseFile file = new ParseFile(ImageParam + ".png", (byte[]) imageObject);
                            // Upload the image into Parse Cloud
                            file.saveInBackground();
                            try {
                                file.save();
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }

                            newAnimal.put(ImageParam, file);

                        }
                    }
                }
            }
        });


        newAnimal.put("Owner", ParseUser.getCurrentUser());
        newAnimal.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressDialogUtil.dismissDialog();
                if (e == null) {
                    sentLatLong();
                } else {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(AddAnimalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sentLatLong() {
        progressDialogUtil.showDialog();
        AndroidNetworking.post("https://www.evacu.pet/alert-now/sendEmailByLatlogn.php")
                .addBodyParameter("userFirstName", ParseUser.getCurrentUser().getUsername())
                .addBodyParameter("email", ParseUser.getCurrentUser().getEmail())
                .addBodyParameter("Fire-Lat", String.valueOf(latitude))
                .addBodyParameter("Fire-Lng", String.valueOf(longitude))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "email = " + response.toString());
                        String status = response.optString("status");
                        String msg = response.optString("Result");
                        Intent intent = new Intent(AddAnimalActivity.this, AnimalsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, anError.getMessage());
                        progressDialogUtil.dismissDialog();
                        Intent intent = new Intent(AddAnimalActivity.this, AnimalsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.other) {
            dialogOtherSpecies();
        } else {
            species = String.valueOf(menuItem.getTitle());
            tvSpecies.setText(species);
        }
        return true;
    }

    public void dialogOtherSpecies() {
        // custom dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogview = inflater.inflate(R.layout.dialog_other_species, null);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
        dialogbuilder.setView(dialogview);
        final AlertDialog alertDialog = dialogbuilder.create();
        final Button btnYes = dialogview.findViewById(R.id.btn_yes);
        final Button btn_close = dialogview.findViewById(R.id.btn_close);
        final EditText edOther = dialogview.findViewById(R.id.ed_other);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(edOther.getText().toString())) {
                    species = edOther.getText().toString();
                    tvSpecies.setText(species);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(AddAnimalActivity.this, "Please add other species", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void setImageAdapter() {
        imageAdapter = new AnimalImageAdapter(this);
        rvAnimal.setLayoutManager(new GridLayoutManager(this, 3));
        rvAnimal.setAdapter(imageAdapter);
    }
}