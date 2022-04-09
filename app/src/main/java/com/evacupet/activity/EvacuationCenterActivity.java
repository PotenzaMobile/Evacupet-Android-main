package com.evacupet.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.AnimalCapacityAdapter;
import com.evacupet.model.AnimalCapacityModel;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.here.android.mpa.guidance.NavigationManager;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvacuationCenterActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = EvacuationCenterActivity.class.getSimpleName();
    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.ed_address)
    EditText edAddress;
    @BindView(R.id.ed_city)
    EditText edCity;
    @BindView(R.id.sp_state)
    Spinner spState;
    @BindView(R.id.ed_zipcode)
    EditText edZipcode;
    @BindView(R.id.ed_center_notes)
    EditText edCenterNotes;
    @BindView(R.id.ed_contact)
    EditText edContact;
    @BindView(R.id.ed_phone_number)
    EditText edPhoneNumber;
    @BindView(R.id.ed_manager_name)
    EditText edManagerName;
    @BindView(R.id.rv_animal_capacity)
    RecyclerView rvAnimalCapacity;
    @BindView(R.id.btn_update)
    Button btnUpdate;
    private ArrayList<AnimalCapacityModel> animalList;
    private ProgressDialogUtil progressDialogUtil;
    private ParseObject updateLocation;
    private boolean isCheckCapacity = false;
    private String state;

    private ParseObject selectedCenter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.evacuation_center));
        getLayoutInflater().inflate(R.layout.activity_evacuation_center, contentFrameLayout);
        ButterKnife.bind(this);
        init();

        selectedCenter = getIntent().getParcelableExtra(Constant.VIEW_LOCATION);
        loadEvacuation(selectedCenter);
    }

    private void loadEvacuation(ParseObject center){

        if(center != null ){

            edName.setText(center.get("Name").toString());

            edManagerName.setText(center.get("centermanager").toString());
            edAddress.setText(center.get("Address").toString());
            edCity.setText(center.get("City").toString());

            //edCity.setText(center.get("State").toString());

            edContact.setText(center.get("alternativeMobile").toString());
            edCenterNotes.setText(center.get("CenterNotes").toString());
            edZipcode.setText(center.get("ZipCode").toString());
            edPhoneNumber.setText(center.get("Phone").toString());

            spState.setSelection(2);

            int i=0;
            for( String s: getResources().getStringArray(R.array.state_list)){
                if(center.get("State").equals(s)){
                    spState.setSelection(i);
                }
                i++;
            }


            for (AnimalCapacityModel animalCapacityModel : animalList) {
                    Object cc = center.get(animalCapacityModel.getKey());
                    String cap = "";
                    if(cc!=null){
                        cap = cc.toString();
                    }
                    animalCapacityModel.setCap(cap);
                    animalList.get(animalList.indexOf(animalCapacityModel)).setCap(cap);
            }

            AnimalCapacityAdapter animalCapacityAdapter = new AnimalCapacityAdapter(this);
            animalCapacityAdapter.setData(animalList);
            rvAnimalCapacity.setLayoutManager(new LinearLayoutManager(this));
            rvAnimalCapacity.setAdapter(animalCapacityAdapter);
            animalCapacityAdapter.notifyDataSetChanged();

        }

    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        setAnimalsCapacityAdapter();
        btnUpdate.setOnClickListener(this);
        setStateAdapter();

        com.evacupet.utility.UsPhoneNumberFormatter addLineNumberFormatter = new com.evacupet.utility.UsPhoneNumberFormatter(
                new WeakReference<EditText>(edPhoneNumber));
        edPhoneNumber.addTextChangedListener(addLineNumberFormatter);

        com.evacupet.utility.UsPhoneNumberFormatter addLineNumberFormatterD = new com.evacupet.utility.UsPhoneNumberFormatter(
                new WeakReference<EditText>(edContact));
        edContact.addTextChangedListener(addLineNumberFormatterD);


    }



    @Override
    public void onImageSuccess(Intent data) {

    }

    private void setAnimalsCapacityAdapter() {
        animalList = new ArrayList<>();
        animalList.add(new AnimalCapacityModel(getString(R.string.horse_capacity), "HorseCapacity", R.drawable.ic_horse));
        animalList.add(new AnimalCapacityModel(getString(R.string.cow_capacity), "CowCapacity", R.drawable.ic_cow));
        animalList.add(new AnimalCapacityModel(getString(R.string.sheep_capacity), "SheepCapacity", R.drawable.ic_sheep));
        animalList.add(new AnimalCapacityModel(getString(R.string.pig_capacity), "PigCapacity", R.drawable.ic_pig));
        animalList.add(new AnimalCapacityModel(getString(R.string.goat_capacity), "GoatCapacity", R.drawable.ic_goat));
        animalList.add(new AnimalCapacityModel(getString(R.string.poultry_capacity), "ChickenCapacity", R.drawable.ic_chicken));
        animalList.add(new AnimalCapacityModel(getString(R.string.dog_capacity), "DogCapacity", R.drawable.ic_dog));
        animalList.add(new AnimalCapacityModel(getString(R.string.cat_capacity), "CatCapacity", R.drawable.ic_cat));
        animalList.add(new AnimalCapacityModel(getString(R.string.reptile_capacity), "Reptile", R.drawable.ic_reptile));
        animalList.add(new AnimalCapacityModel(getString(R.string.bird_capacity), "Bird", R.drawable.ic_bird));
        animalList.add(new AnimalCapacityModel(getString(R.string.pocket_pet_capacity), "Pocketpet", R.drawable.ic_pocket_pet));
        animalList.add(new AnimalCapacityModel(getString(R.string.small_pet_capacity), "BunnyCapacity", R.drawable.ic_bunny));
        animalList.add(new AnimalCapacityModel(getString(R.string.other_capacity), "Other", R.drawable.ic_dog));
        AnimalCapacityAdapter animalCapacityAdapter = new AnimalCapacityAdapter(this);
        animalCapacityAdapter.setData(animalList);
        rvAnimalCapacity.setLayoutManager(new LinearLayoutManager(this));
        rvAnimalCapacity.setAdapter(animalCapacityAdapter);
    }

    private void updateCenterLocation() {
        String fullAddress;


        if(selectedCenter!=null){
            updateLocation = selectedCenter;
        }else{
            updateLocation = new ParseObject("EvacCenter");
        }

        updateLocation.put("Name", edName.getText().toString());
        updateLocation.put("centermanager", edManagerName.getText().toString());
        updateLocation.put("Address", edAddress.getText().toString());
        updateLocation.put("City", edCity.getText().toString());
        if (!state.equals("Select state")) {
            updateLocation.put("State", state);
            fullAddress = edAddress.getText().toString() + " " + edCity.getText().toString() + "," + state + " " + edZipcode.getText().toString();

        } else {
            updateLocation.put("State", "");
            fullAddress = edAddress.getText().toString() + " " + edCity.getText().toString() + "," + edZipcode.getText().toString();

        }
        updateLocation.put("alternativeMobile", edContact.getText().toString());
        updateLocation.put("CenterNotes", edCenterNotes.getText().toString());
        updateLocation.put("ZipCode", edZipcode.getText().toString());
        updateLocation.put("Phone", edPhoneNumber.getText().toString());
        updateLocation.put("Owner", ParseUser.getCurrentUser());
        updateLocation.put("EvId", BaseUtility.currentDateTime());
        for (AnimalCapacityModel animalCapacityModel : animalList) {
            if (!TextUtils.isEmpty(animalCapacityModel.getCap())) {
                updateLocation.put(animalCapacityModel.getKey(), Integer.parseInt(animalCapacityModel.getCap()));
            }
        }

        try {
            JSONObject jsonObject = new BaseUtility().parseObjectToJson(updateLocation);
            Log.e("TAG", jsonObject.toString(4));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final ParseGeoPoint geoPoint = getLocationFromAddress(fullAddress);

        updateLocation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressDialogUtil.dismissDialog();
                if (e == null) {
                    if (geoPoint != null) {
                        updateLocation.put("LocationGPS", geoPoint);
                    }
                    updateLocation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Intent intent = new Intent(EvacuationCenterActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });

                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void setStateAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.state_list));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spState.setAdapter(adapter);
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = adapter.getItem(position);
                //Log.e(TAG, state + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_update:
                capacityValidation();
                if (TextUtils.isEmpty(edManagerName.getText().toString())) {
                    Toast.makeText(this, "Please enter center manager name", Toast.LENGTH_SHORT).show();
                } else if (!isCheckCapacity) {
                    Toast.makeText(this, "Please enter animal capacity", Toast.LENGTH_SHORT).show();
                } else {
                    if (ConnectionUtil.isInternetOn(this)) {
                        progressDialogUtil.showDialog();
                        updateCenterLocation();
                    } else {
                        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }
    }


    private ParseGeoPoint getLocationFromAddress(String strAddress) {
        ParseGeoPoint geo = null;
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 10);
            Address location = address.get(0);
            geo = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {

        }
        return geo;
    }

    private void capacityValidation() {
        for (AnimalCapacityModel animalCapacityModel : animalList) {
            if (!TextUtils.isEmpty(animalCapacityModel.getCap())) {
                isCheckCapacity = true;
                break;
            }
        }
    }
}
