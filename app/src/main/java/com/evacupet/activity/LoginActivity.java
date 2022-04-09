package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evacupet.R;
import com.evacupet.utility.ConnectionUtil;

import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.SessionManager;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_sign_up)
    Button btnSignUp;
    @BindView(R.id.ed_username)
    EditText edUsername;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.tv_forgot)
    TextView tvForgot;
    private ProgressDialogUtil progressDialogUtil;
    private boolean isTermsCheck  = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        tvForgot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                //  sentToDashboardActivity();
                attemptLogin();
                break;
            case R.id.btn_sign_up:
                sentToSignUpActivity();
                break;
            case R.id.tv_forgot:
                sentToForgotPasswordActivity();
                break;
        }
    }

    private void attemptLogin() {
        edUsername.setError(null);
        edPassword.setError(null);
        boolean cancel = false;
        View focusView = null;

        String username = edUsername.getText().toString().trim();
        final String password = edPassword.getText().toString().trim();

        // Check user entered password.
        if (TextUtils.isEmpty(password)) {
            edPassword.setError(getString(R.string.error_field_required));
            focusView = edPassword;
            cancel = true;
        }
        // Check for a valid mobile address.
        if (TextUtils.isEmpty(username)) {
            edUsername.setError(getString(R.string.error_field_required));
            focusView = edUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            if (ConnectionUtil.isInternetOn(this)) {
                progressDialogUtil.showDialog();
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            new SessionManager(LoginActivity.this).saveUserDetail(parseUser.getUsername(), parseUser.getSessionToken());
                            sentToDashboardActivity();
                            Log.e("session = ", parseUser.getSessionToken()+"");
                            progressDialogUtil.dismissDialog();

                        } else {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialogUtil.dismissDialog();

                        }
                    }
                });
            } else {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sentToDashboardActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sentToSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void sentToForgotPasswordActivity() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}
