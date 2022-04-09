package com.evacupet.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.adapter.EvacuateNowAdapter;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvacuateNowActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = EvacuateNowActivity.class.getSimpleName();
    @BindView(R.id.rv_animal)
    RecyclerView rvAnimal;
    @BindView(R.id.btn_ev_selected)
    Button btnEvSelected;
    @BindView(R.id.btn_ev_no_needed)
    Button btnEvNoNeeded;
    private ProgressDialogUtil progressDialogUtil;
    private List<ParseObject> mSelected;
    private EvacuateNowAdapter animalListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.evacuate_now));
        getLayoutInflater().inflate(R.layout.activity_evacuate_now, contentFrameLayout);
        ButterKnife.bind(this);
        init();

    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnEvNoNeeded.setOnClickListener(this);
        btnEvSelected.setOnClickListener(this);
        progressDialogUtil.showDialog();
        setAdapter();
        getAnimals();
    }

    private void getAnimals() {
        mSelected = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Animals");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    mSelected = objects;
                    for (ParseObject object : mSelected) {
                        if (!object.has("Status") || object.getInt("Status") == 0) {
                            object.put("Status", 1);
                        }
                    }
                    animalListAdapter.setData(mSelected);
                    animalListAdapter.notifyDataSetChanged();
                    progressDialogUtil.dismissDialog();
                } else {
                    progressDialogUtil.dismissDialog();
                    Log.e("animal error = ", e.getMessage());
                }

            }
        });
    }

    private void setAdapter() {
        animalListAdapter = new EvacuateNowAdapter(EvacuateNowActivity.this);
        rvAnimal.setLayoutManager(new LinearLayoutManager(EvacuateNowActivity.this));
        rvAnimal.setAdapter(animalListAdapter);
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ev_no_needed:
                noNeededButton();
                break;
            case R.id.btn_ev_selected:
                btnEvNoNeededClick();
                break;
        }
    }

    private void noNeededButton() {
      //  progressDialogUtil.showDialog();
        ParseUser parseUser = ParseUser.getCurrentUser();
        JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", parseUser.getObjectId());
            object.putOpt("session_id", parseUser.getSessionToken());
            Log.e(TAG, object + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post("https://evacu.pet/alert-now/location_cancel.php")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                      //  progressDialogUtil.dismissDialog();
                        Log.e(TAG, response.toString());

                    }

                    @Override
                    public void onError(ANError anError) {
                      //  progressDialogUtil.dismissDialog();
                        Log.e(TAG, anError.getMessage());
                    }
                });

        Intent intent = new Intent(EvacuateNowActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void btnEvNoNeededClick() {

        int selectedCount = 0;
        for (ParseObject object : mSelected) {
            if(object.getInt("Status")==1){
                selectedCount++;
            }
        }
        if(selectedCount>1){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("").setMessage("You can select only one animal to evacute now")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
            return;
        }

        for (ParseObject object : mSelected) {
            Log.e(TAG, object.getInt("Status") + "");
            if (object.has("Status")) {
                object.put("Status", object.getInt("Status"));
                object.saveInBackground();
            }
        }
        Intent intent = new Intent(this, RequestEvacuationActivity.class);
        startActivity(intent);
        finish();
    }
}
