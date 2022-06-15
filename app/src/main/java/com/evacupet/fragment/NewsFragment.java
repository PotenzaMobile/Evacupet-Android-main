package com.evacupet.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.activity.ChatActivity;
import com.evacupet.activity.DashboardActivity;
import com.evacupet.activity.EvacuateNowActivity;
import com.evacupet.activity.EvacuationProgressActivity;
import com.evacupet.activity.SplashActivity;
import com.evacupet.activity.TrackAnimalsActivity;
import com.evacupet.model.AnimalListModel;
import com.evacupet.model.NewsModel;
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
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.evacupet.adapter.NewsListRecyclerViewAdapter;


public class NewsFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = NewsFragment.class.getSimpleName();
    @BindView(R.id.btn_cancel_ev)
    Button btnCancelEv;
    @BindView(R.id.btn_access)
    Button btnAccess;
    @BindView(R.id.btn_go_center)
    Button btnGoCenter;
    @BindView(R.id.btn_find_animal)
    Button btnFindAnimal;
    @BindView(R.id.btn_chat)
    Button btnChat;
    @BindView(R.id.tv_news_title)
    TextView tvNewsTitle;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.iv_go_close)
    ImageView ivGoClose;
    @BindView(R.id.rl_dialog)
    RelativeLayout rlDialog;
    @BindView(R.id.rl_center_dialog)
    RelativeLayout rlCenterDialog;
    @BindView(R.id.ll_track_animal)
    LinearLayout llTrackAnimal;

    @BindView(R.id.news_list)
    RecyclerView newsList;

    private boolean inEvac;
    private int animalsInTransit = 0;
    private int animalsEvacuated = 0;
    private double latitude = 0.0, longitude;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private ProgressDialogUtil progressDialogUtil;
    private ParseObject animalObject = null;

    ArrayList<NewsModel> newsData = new ArrayList<NewsModel>();
    NewsListRecyclerViewAdapter newsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, view);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        init();
        return view;
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(getActivity());
        btnCancelEv.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        ivGoClose.setOnClickListener(this);
        btnGoCenter.setOnClickListener(this);
        btnFindAnimal.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        btnAccess.setOnClickListener(this);
        getAllAnimals();
        checkCenter();
        checkEvacuation();
        checkVolunteerChat();
        getAllNews();
    }

    private void getAllNews() {
        progressDialogUtil.showDialog();
        if(SplashActivity.newsData.size()==0) {


            ParseQuery<ParseObject> query = ParseQuery.getQuery("News");
            query.orderByDescending("createAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    progressDialogUtil.dismissDialog();
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object1 : objects) {
                                Date date = object1.getCreatedAt();
                                android.text.format.DateFormat df = new android.text.format.DateFormat();
                                newsData.add(new NewsModel(10, df.format("hh:mm a dd, MMM", date).toString(), object1.getString("title"), object1.getString("shortDescription"), object1.getString("description"), object1.getParseFile("image").getUrl()));
                            }
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    NewsModel[] nm = newsData.toArray(new NewsModel[newsData.size()]);
                                    newsAdapter = new NewsListRecyclerViewAdapter(nm);
                                    newsList.setHasFixedSize(true);
                                    newsList.setLayoutManager(new LinearLayoutManager(getContext()));
                                    newsList.setAdapter(newsAdapter);
                                }
                            });
                        } else {

                        }

                    } else {
                        //Log.e("error = ", e.getMessage());
                    }
                }
            });


            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    newsData = SplashActivity.newsData;
                    NewsModel[] nm = newsData.toArray(new NewsModel[newsData.size()]);
                    newsAdapter = new NewsListRecyclerViewAdapter(nm);
                    newsList.setHasFixedSize(true);
                    newsList.setLayoutManager(new LinearLayoutManager(getContext()));
                    newsList.setAdapter(newsAdapter);
                }
            });


        }else {

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    newsData = SplashActivity.newsData;
                    NewsModel[] nm = newsData.toArray(new NewsModel[newsData.size()]);
                    newsAdapter = new NewsListRecyclerViewAdapter(nm);
                    newsList.setHasFixedSize(true);
                    newsList.setLayoutManager(new LinearLayoutManager(getContext()));
                    newsList.setAdapter(newsAdapter);
                }
            });
        }
    }

    private void checkCenter() {
        final ParseQuery animalQuery = new ParseQuery("Animals");
        animalQuery.whereEqualTo("Status", 2);
        animalQuery.whereEqualTo("EvacuatedBy", ParseUser.getCurrentUser());
        animalQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    if (count > 0) {
                        rlCenterDialog.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void checkVolunteerChat() {
        ParseQuery<ParseObject> animalQuery = ParseQuery.getQuery("Animals");
        animalQuery.whereEqualTo("EvacuatedBy", ParseUser.getCurrentUser());
        animalQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {

                        for (ParseObject object : objects) {

                            if (object.has("Status")) {
                                if (object.getInt("Status") == 2 || object.getInt("Status") == 3 || object.getInt("Status") == 4) {
                                    btnChat.setVisibility(View.VISIBLE);
                                    llTrackAnimal.setVisibility(View.VISIBLE);
                                }
                                animalObject = object;
                                break;
                            }
                        }
                    }

                } else {
                    Log.e("error = ", e.getMessage());
                }
            }
        });

    }

    private void checkEvacuation() {
        ParseQuery<ParseObject> evacQuery = new ParseQuery<>("Evacuations");
        evacQuery.whereEqualTo("UserProperty", ParseUser.getCurrentUser());
        evacQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects != null && objects.size() > 0) {
                        int animalCount = 0;

                        animalCount = ParseUser.getCurrentUser().getInt("animalCount");
                        if (animalCount > 0) {
                            inEvac = false;
                            for (ParseObject parseObject : objects) {
                                if (parseObject.getBoolean("needEvacuation")) {
                                    inEvac = true;
                                }
                            }
                            rlDialog.setVisibility(View.VISIBLE);
                            btnCancelEv.setVisibility(View.VISIBLE);
                            if (inEvac) {
                                ParseQuery<ParseObject> animalQuery = new ParseQuery<>("Animals");
                                animalQuery.whereEqualTo("Owner", ParseUser.getCurrentUser());
                                final int finalAnimalCount = animalCount;
                                animalQuery.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objects, ParseException e) {
                                        if (e == null) {
                                            if (objects != null && objects.size() > 0) {
                                                for (ParseObject parseObject : objects) {
                                                    int status = parseObject.getInt("Status");
                                                    if (status == 2) {
                                                        animalsInTransit = animalsInTransit + 1;
                                                    }
                                                    if (status == 3) {
                                                        animalsEvacuated = animalsEvacuated + 1;
                                                    }
                                                }
                                            }
                                            tvNewsTitle.setText("Your location is currently under evacuation. Of your " + finalAnimalCount + " animals, " + animalsInTransit + " are in transit, and " + animalsEvacuated + " have already been evacuated.");
                                            btnCancelEv.setText("Modify Evacuation");
                                           // checkAccess();
                                            btnAccess.setVisibility(View.GONE);
                                        } else {
                                            Log.e(TAG, " qq = " + e.getMessage());
                                        }
                                    }
                                });
                            } else {
                                checkAccess();
                                tvNewsTitle.setText("Your location is currently under evacuation. Do you need your animals evacuated?");
                                btnCancelEv.setText("Request Evacuation");
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void checkAccess() {
        ParseUser user = ParseUser.getCurrentUser();
        int isAccess = user.getInt("admin_status");
        if (isAccess == 1) {
            btnAccess.setText(R.string.not_accessible);
        } else {
            btnAccess.setText(R.string.accessible);
        }
        btnAccess.setVisibility(View.VISIBLE);

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

                        for (ParseObject object : objects) {
                            if (object.has("Status")) {
                                if (object.getInt("Status") == -1 || object.getInt("Status") == 2 || object.getInt("Status") == 3) {
                                    llTrackAnimal.setVisibility(View.VISIBLE);
                                    btnFindAnimal.setVisibility(View.VISIBLE);
                                    animalObject = object;
                                }
                                if (object.getInt("Status") == 2 || object.getInt("Status") == 3 || object.getInt("Status") == 4) {
                                    llTrackAnimal.setVisibility(View.VISIBLE);
                                    btnChat.setVisibility(View.VISIBLE);
                                    animalObject = object;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go_center:
                sendLocation();
                break;
            case R.id.iv_close:
                rlDialog.setVisibility(View.GONE);
                break;
            case R.id.iv_go_close:
                rlCenterDialog.setVisibility(View.GONE);
                break;
            case R.id.btn_cancel_ev:
                Intent intent = new Intent(getContext(), EvacuateNowActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_find_animal:
                Intent intent1 = new Intent(getContext(), TrackAnimalsActivity.class);
                intent1.putExtra(Constant.TRACk_ANIMAL, animalObject);
                startActivity(intent1);
                break;
            case R.id.btn_chat:
                Intent intent2 = new Intent(getContext(), ChatActivity.class);
                intent2.putExtra(Constant.TRACk_ANIMAL, animalObject);
                startActivity(intent2);
                break;
            case R.id.btn_access:
                ParseUser user = ParseUser.getCurrentUser();
                if (btnAccess.getText().toString().equals("Not accessible")) {
                    user.put("admin_status", 0);
                } else {
                    user.put("admin_status", 1);
                }
                user.saveInBackground();
                checkAccess();
                break;
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
                            latitude = newLocation.getLatitude();
                            longitude = newLocation.getLongitude();

                        } else {
                            trackLocation();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (new LocationOnUtility(getContext(), getActivity()).enableLocation()) {
            if (latitude == 0.0) {
                trackLocation();
            } else {
                if (mFusedLocationProviderClient != null && mLocationRequest != null) {
                    mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFusedLocationProviderClient != null && mLocationRequest != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationProviderClient != null && mLocationRequest != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void sendLocation() {
        progressDialogUtil.showDialog();
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
                        Log.e(TAG, response.toString());
                        String found_location = response.optString("found_location");
                        if (!TextUtils.isEmpty(found_location)) {
                            double foundLatitude = response.optDouble("Found_Lat");
                            double foundLongitude = response.optDouble("Found_Lon");
                            Intent intent = new Intent(getContext(), EvacuationProgressActivity.class);
                            intent.putExtra(Constant.FOUND_LAT, foundLatitude);
                            intent.putExtra(Constant.FOUND_LONG, foundLongitude);
                            intent.putExtra(Constant.FOUND_LOCATION, found_location);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), response.optString("Result"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, anError.getMessage());
                    }
                });

    }
}