package com.evacupet.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evacupet.R;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.Validation;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.btn_send_link)
    Button btnSendLink;

    @BindView(R.id.btn_back)
    Button btnBack;

    private ProgressDialogUtil progressDialogUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
        init();
    }
    private void init(){
        progressDialogUtil = new ProgressDialogUtil(this);
        btnSendLink.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send_link:
                attemptForgot();
                break;
            case R.id.btn_back:
                onBackPressed();
                break;
        }
    }

    private void attemptForgot() {
        edEmail.setError(null);
        boolean cancel = false;
        View focusView = null;
        String email = edEmail.getText().toString().trim();
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            edEmail.setError(getString(R.string.error_field_required));
            focusView = edEmail;
            cancel = true;
        } else if (!Validation.isValidEmail(email)) {
            edEmail.setError(getString(R.string.error_invalid_email));
            focusView = edEmail;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if (ConnectionUtil.isInternetOn(this)) {
                progressDialogUtil.showDialog();
                forgotApi(email);
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void forgotApi(String email){
        ParseUser.requestPasswordResetInBackground(email,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        progressDialogUtil.dismissDialog();
                        if (e == null) {
                           onBackPressed();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }
}
