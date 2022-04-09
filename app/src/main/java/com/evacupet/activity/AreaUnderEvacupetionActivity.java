package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;

import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AreaUnderEvacupetionActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = AreaUnderEvacupetionActivity.class.getSimpleName();
    @BindView(R.id.btn_no)
    Button btnNo;
    @BindView(R.id.btn_yes)
    Button btnYes;
    ProgressDialogUtil progressDialogUtil;
    private int flagAccept = 0;
    private String randomNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText("");
        getLayoutInflater().inflate(R.layout.activity_area_under_evacupetion, contentFrameLayout);
        ButterKnife.bind(this);
        init();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnNo.setOnClickListener(this);
        btnYes.setOnClickListener(this);
        if (getIntent().hasExtra("shareLink")) {
            randomNumber = getIntent().getStringExtra("shareLink");
            Log.e("ShareLink = ", randomNumber);
        }
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                flagAccept = 1;
                if (ConnectionUtil.isInternetOn(this)) {
                    acceptAndCancelApi();
                } else {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_no:
                flagAccept = 2;
                Intent intent = new Intent(AreaUnderEvacupetionActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
               /* if (ConnectionUtil.isInternetOn(this)) {
                    acceptAndCancelApi();
                } else {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }*/
                break;
        }
    }

    private void acceptAndCancelApi() {
        progressDialogUtil.showDialog();
        ParseUser parseUser = ParseUser.getCurrentUser();
       /* JSONObject object = new JSONObject();
        try {
            object.putOpt("user_id", parseUser.getObjectId());
            object.putOpt("number", randomNumber);
            object.putOpt("accept_flage", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
*/
        AndroidNetworking.post("https://www.evacu.pet/alert-now/accept_request.php")
                .addBodyParameter("user_id", parseUser.getObjectId())
                .addBodyParameter("number", randomNumber)
                .addBodyParameter("accept_flage", "1")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        String status = response.optString("status");
                        Log.e(TAG, "accept =" + response.toString());
                        if (!status.equals("Fail")) {
                            if (flagAccept == 1) {
                                Intent intent = new Intent(AreaUnderEvacupetionActivity.this, FindEvacationActivity.class);
                                intent.putExtra(Constant.NOTIFICATION_FLAG, Constant.NOTIFICATION_FLAG);
                                startActivity(intent);
                                finish();
                            }
                        }else {
                            Toast.makeText(AreaUnderEvacupetionActivity.this, response.optString("Result"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AreaUnderEvacupetionActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
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