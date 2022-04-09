package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.utility.LocationOnUtility;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestEvacuationActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = RequestEvacuationActivity.class.getSimpleName();
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_des)
    TextView tvDes;
    @BindView(R.id.btn_done)
    Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.evacuate_now));
        getLayoutInflater().inflate(R.layout.activity_request_evacuation, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        btnDone.setOnClickListener(this);
        contactingServer();

    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_done:
                Intent intent = new Intent(RequestEvacuationActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void contactingServer() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", parseUser.getObjectId());
            object.putOpt("session_id", parseUser.getSessionToken());
            Log.e(TAG, object + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post("https://evacu.pet/alert-now/evacuate.php")
                .addJSONObjectBody(object)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response + "");
                        String result  = response.optString("Result");
                        if (!TextUtils.isEmpty(result)) {
                            tvTitle.setText(getString(R.string.ev_request));
                            tvDes.setText(getString(R.string.ev_request_msg));
                            btnDone.setVisibility(View.VISIBLE);
                            new LocationOnUtility(RequestEvacuationActivity.this, RequestEvacuationActivity.this).enableLocation();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        Log.e(TAG, anError.getMessage());
                    }
                });

    }
}
