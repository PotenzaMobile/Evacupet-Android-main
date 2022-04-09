package com.evacupet.fragment;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.LocationAdapter;
import com.evacupet.interfaceHelper.AddLocationClick;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LocationFragment extends Fragment implements View.OnClickListener {
    @BindView(R.id.ed_address)
    EditText edAddress;
    @BindView(R.id.ed_city)
    EditText edCity;
    @BindView(R.id.ed_state)
    EditText edState;
    @BindView(R.id.ed_zipcode)
    EditText edZipcode;
    @BindView(R.id.rl_add_location)
    RelativeLayout rlAddLocation;
    @BindView(R.id.rl_location_list)
    RelativeLayout rlLocationList;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.btn_add_location)
    Button btnAddLocation;
    @BindView(R.id.lv_locations)
    RecyclerView lvLocations;
    private double latitude, longitude;
    private int flag;
    private ParseObject locationObject;
    private AddLocationClick itemClick;
    private RelativeLayout locationView;

    public void setFlag(AddLocationClick itemClick,RelativeLayout locationView,int flag){
        this.itemClick = itemClick;
        this.flag = flag;
        this.locationView = locationView;
    }
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_location, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        btnAddLocation.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        getLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_location:
                rlAddLocation.setVisibility(View.VISIBLE);
                rlLocationList.setVisibility(View.GONE);
                locationView.setVisibility(View.GONE);

                break;
            case R.id.btn_cancel:
                rlAddLocation.setVisibility(View.GONE);
                rlLocationList.setVisibility(View.VISIBLE);
                locationView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_add:
                addLocation();
                break;
        }
    }

    private void getLocation() {
        final ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Locations");
            parseQuery.whereEqualTo("Owner", user);
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects != null && objects.size() > 0) {
                            LocationAdapter adapter = new LocationAdapter(getContext(), objects,itemClick,flag);
                            lvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
                            lvLocations.setAdapter(adapter);
                        }
                    } else {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    private void addLocation() {
        String city = edCity.getText().toString();
        String address = edAddress.getText().toString();
        String zipcode = edZipcode.getText().toString();
        String state = edState.getText().toString();
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            Log.e("no user", "");
        }
        getLocationFromAddress(address + " " + city + "," + state + " " + zipcode);
        ParseGeoPoint geo = new ParseGeoPoint(latitude, longitude);
        ParseObject object = new ParseObject("Locations");

        object.put("address", address);
        object.put("city", city);
        object.put("state", state);
        object.put("zip", zipcode);

        object.put("Owner", user);
        object.put("GPS", geo);
        object.put("isMain", 1);
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    rlLocationList.setVisibility(View.VISIBLE);
                    locationView.setVisibility(View.VISIBLE);
                    rlAddLocation.setVisibility(View.GONE);
                    getLocation();
                } else {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(getContext());
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 10);
            Address location = address.get(0);
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        } catch (Exception e) {

        }
    }


}
