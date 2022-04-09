package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.ParseUser;


import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportEmergencyActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG  = ReportEmergencyActivity.class.getSimpleName();
    @BindView(R.id.ed_description)
    EditText edDescription;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content);
        getLayoutInflater().inflate(R.layout.activity_report_emergency, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        titleName.setText(getString(R.string.report_an_emergency));
        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onImageSuccess(Intent data) {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit) {
            attemptReport();
        }
    }

    private void attemptReport() {
        edDescription.setError(null);
        String description = edDescription.getText().toString().trim();
        if (TextUtils.isEmpty(description)) {
            edDescription.setError(getString(R.string.error_field_required));
        } else {
            if (ConnectionUtil.isInternetOn(this)) {
                sendMail(description);
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendMail(String description) {
        progressDialogUtil.showDialog();
        AndroidNetworking.post("https://evacu.pet/alert-now/suggestion.php")
                .addBodyParameter("email", ParseUser.getCurrentUser().getEmail())
                .addBodyParameter("text", description)
                .addBodyParameter("flage", "2")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "email = " + response.toString());
                        String status = response.optString("status");
                        String msg = response.optString("Result");
                        Toast.makeText(ReportEmergencyActivity.this, msg, Toast.LENGTH_SHORT).show();
                        if (status.equals("Pass")) {
                            edDescription.setText("");
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialogUtil.dismissDialog();
                    }
                });
    }
}