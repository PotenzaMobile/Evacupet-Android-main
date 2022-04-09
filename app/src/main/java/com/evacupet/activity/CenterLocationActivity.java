package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.CenterLocationAdapter;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CenterLocationActivity extends DashboardActivity {
    private static final String TAG = CenterLocationActivity.class.getSimpleName();
    @BindView(R.id.rv_center_location)
    RecyclerView rvCenterLocation;
    private CenterLocationAdapter adapter;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.center_location));
        getLayoutInflater().inflate(R.layout.activity_center_location, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setAdapter();
        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.showDialog();
        getCenterLocationList();
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    private void getCenterLocationList() {
        ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("EvacCenter");
        parseQuery.whereEqualTo("Owner", ParseUser.getCurrentUser());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialogUtil.dismissDialog();
                if (e == null) {
                    adapter.setData(objects);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setAdapter() {
        adapter = new CenterLocationAdapter(CenterLocationActivity.this);
        rvCenterLocation.setLayoutManager(new LinearLayoutManager(CenterLocationActivity.this));
        rvCenterLocation.setAdapter(adapter);
    }
}
