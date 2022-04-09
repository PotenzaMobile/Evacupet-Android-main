package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.EvacuationsAdapter;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvacuationsActivity extends DashboardActivity {

    private static final String TAG = EvacuationsActivity.class.getSimpleName();
    @BindView(R.id.rv_evacuations)
    RecyclerView rvEvacuations;
    private EvacuationsAdapter adapter;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.evacuation_eva));
        getLayoutInflater().inflate(R.layout.activity_evacuations_list, contentFrameLayout);
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

        /*
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
                //Gson gson = new Gson();
                //Log.d("aaa:",gson.toJson(objects).toString());
            }
        });
        */


        final ArrayList<ParseObject> result = new ArrayList<ParseObject>();
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("EvacuationGroup");
        //parseQuery.whereEqualTo("Owner", ParseUser.getCurrentUser());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialogUtil.dismissDialog();
                if(e==null){
                    for (ParseObject p:objects){
                        final ParseQuery<ParseObject> evacuationQuery = new ParseQuery<ParseObject>("Evacuations");
                        evacuationQuery.whereEqualTo("Group", p);
                        //evacuationQuery.whereEqualTo("UserProperty", ParseUser.getCurrentUser().getObjectId());
                        evacuationQuery.include("UserProperty");
                        evacuationQuery.include("UserLocation");
                        evacuationQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> evacuationsObjects, ParseException e) {

                                if(e==null){

                                    for(ParseObject eo: evacuationsObjects){
                                        ParseObject po = new ParseObject("EvacCenter");
                                        po.put("Name",eo.getParseObject("UserProperty").getString("FirstName")+" "+eo.getParseObject("UserProperty").getString("LastName"));

                                        //po.put("Address",eo.getParseObject("UserLocation").getString("address"));
                                        //po.put("City",eo.getParseObject("UserLocation").getString("city"));
                                        //po.put("State",eo.getParseObject("UserLocation").getString("state"));
                                        //po.put("ZipCode",eo.getParseObject("UserLocation").getString("zip"));

                                        po.put("Address",eo.getParseObject("UserProperty").getString("Address"));
                                        po.put("City",eo.getParseObject("UserProperty").getString("City"));
                                        String state = eo.getParseObject("UserProperty").getString("State");
                                        if(state==null){
                                            state = "";
                                        }
                                        po.put("State", state );
                                        po.put("ZipCode", eo.getParseObject("UserProperty").getString("ZipCode") );

                                        po.put("Volunteer",eo.getParseObject("UserProperty").getBoolean("Volunteer"));
                                        po.put("LocationGPS",eo.getParseObject("UserProperty").getParseGeoPoint("LastLocation"));


                                        result.add(po);
                                        adapter.setData(result);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });



                    }
                }
            }
        });



    }

    private void setAdapter() {
        adapter = new EvacuationsAdapter(EvacuationsActivity.this);
        rvEvacuations.setLayoutManager(new LinearLayoutManager(EvacuationsActivity.this));
        rvEvacuations.setAdapter(adapter);
    }
}
