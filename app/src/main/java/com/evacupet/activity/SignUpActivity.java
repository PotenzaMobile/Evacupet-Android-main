package com.evacupet.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.evacupet.R;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.CompressImageUtility;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.SessionManager;
import com.evacupet.utility.Validation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SignUpActivity.class.getSimpleName();
    private static final int YOUR_SELECT_PICTURE_REQUEST_CODE = 101;
    private static final int ZXING_CAMERA_PERMISSION = 102;
    @BindView(R.id.btn_add_photo)
    Button btnAddPhoto;
    @BindView(R.id.iv_user_image)
    ImageView ivUserImage;
    @BindView(R.id.ed_username)
    EditText edUsername;
    @BindView(R.id.ed_first_name)
    EditText edFirstName;
    @BindView(R.id.ed_last_name)
    EditText edLastName;
    @BindView(R.id.ed_gate_code)
    EditText edGateCode;
    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.ed_phone)
    EditText edPhone;
    @BindView(R.id.ed_address)
    EditText edAddress;
    @BindView(R.id.ed_city)
    EditText edCity;
    @BindView(R.id.ed_zipcode)
    EditText edZipcode;
    @BindView(R.id.ed_note)
    EditText edNote;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.ed_rept_pass)
    EditText edReptPass;
    @BindView(R.id.sp_state)
    Spinner spState;
    @BindView(R.id.sp_group)
    Spinner spGroup;
    @BindView(R.id.rg_sign_up_type)
    RadioGroup rgSignUpType;
    @BindView(R.id.btn_sign_up)
    Button btnSignUp;
    @BindView(R.id.iv_team_cont)
    ImageView ivTeamCont;
    @BindView(R.id.tv_terms_cont)
    TextView tvTermsCont;



    @BindView(R.id.tv_group)
    TextView tvgroup;
    @BindView(R.id.ll_group)
    LinearLayout llGroup;
    @BindView(R.id.view_11)
    View view11;



    private String selectedImagePath;
    private File image;
    private String state;
    private String userGroup;
    private Uri outputFileUri;
    private boolean signUpType = false;
    private ProgressDialogUtil progressDialogUtil;
    private double latitude, longitude;
    private boolean isCheckTerms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnSignUp.setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);
        tvTermsCont.setOnClickListener(this);
        ivTeamCont.setOnClickListener(this);
        setStateAdapter();
        setGroupAdapter();
        rgSignUpType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_yes) {
                    signUpType = true;
                    showUserGroup();
                } else if (checkedId == R.id.rb_no) {
                    signUpType = false;
                    hideUserGroup();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up:
                attemptSignUp();
                break;
            case R.id.btn_add_photo:
                outputFileUri = new BaseUtility().checkAndRequestPermissions(this);
                break;
            case R.id.tv_terms_cont:
                Intent intent = new Intent(SignUpActivity.this, TermsAndConditionsActivity.class);
                startActivity(intent);
            case R.id.iv_team_cont:
                if (ivTeamCont.isSelected()) {
                    isCheckTerms = false;
                    ivTeamCont.setSelected(false);

                } else {
                    isCheckTerms = true;
                    ivTeamCont.setSelected(true);

                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        outputFileUri = new BaseUtility().openImageIntent(this);

                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            new BaseUtility().showDialogOK(SignUpActivity.this, "Camera Permission required for this action",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    outputFileUri = new BaseUtility().checkAndRequestPermissions(SignUpActivity.this);
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                Uri selectedImageUri;
                String path;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                    selectedImagePath = new CompressImageUtility().compressImage(this, selectedImageUri.getPath());
                    //   selectedImagePath = selectedImageUri.getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    ivUserImage.setVisibility(View.VISIBLE);
                    ivUserImage.setImageBitmap(bitmap);

                } else {
                    selectedImageUri = data == null ? null : data.getData();
                    path = new BaseUtility().getPath(this, selectedImageUri);
                    //  selectedImagePath = path;
                    selectedImagePath = new CompressImageUtility().compressImage(this, path);
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    ivUserImage.setVisibility(View.VISIBLE);
                    ivUserImage.setImageBitmap(bitmap);

                }
            }
        }
    }

    private void attemptSignUp() {
        edUsername.setError(null);
        edPassword.setError(null);
        edEmail.setError(null);
        edPhone.setError(null);
        edReptPass.setError(null);
        boolean cancel = false;
        View focusView = null;

        String username = edUsername.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String reptPassword = edReptPass.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String email = edEmail.getText().toString().trim();


        // Check user entered password.
        if (TextUtils.isEmpty(reptPassword)) {
            edReptPass.setError(getString(R.string.error_field_required));
            focusView = edReptPass;
            cancel = true;
        } else if (!reptPassword.equals(password)) {
            edReptPass.setError(getString(R.string.error_password));
            focusView = edReptPass;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            edPassword.setError(getString(R.string.error_field_required));
            focusView = edPassword;
            cancel = true;
        }
        // Check for a valid mobile address.
        if (TextUtils.isEmpty(phone)) {
            edPhone.setError(getString(R.string.error_field_required));
            focusView = edPhone;
            cancel = true;
        } else if (phone.length() < 1) {
            edPhone.setError(getString(R.string.error_invalid_mobile));
            focusView = edPhone;
            cancel = true;
        }
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
            if (isCheckTerms) {
                if (ConnectionUtil.isInternetOn(this)) {
                    progressDialogUtil.showDialog();
                    signUpApi();
                } else {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "You must agree with the Terms and Conditions!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setStateAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.state_list));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spState.setAdapter(adapter);
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setGroupAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.userGroup));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spGroup.setAdapter(adapter);
        spGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userGroup = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void hideUserGroup(){
        tvgroup.setVisibility(View.GONE);
        llGroup.setVisibility(View.GONE);
        view11.setVisibility(View.GONE);
        spGroup.setVisibility(View.GONE);
    }

    private void showUserGroup(){
        tvgroup.setVisibility(View.VISIBLE);
        llGroup.setVisibility(View.VISIBLE);
        view11.setVisibility(View.VISIBLE);
        spGroup.setVisibility(View.VISIBLE);
    }


    private void signUpApi() {
        ParseUser user = new ParseUser();
        user.setUsername(edUsername.getText().toString());
        user.setEmail(edEmail.getText().toString());
        user.setPassword(edPassword.getText().toString());
        user.put("Address", edAddress.getText().toString());
        user.put("MobileNumber", edPhone.getText().toString());
        user.put("City", edCity.getText().toString());
        user.put("ZipCode", edZipcode.getText().toString());
        user.put("FirstName", edFirstName.getText().toString());
        user.put("LastName", edLastName.getText().toString());
        user.put("Volunteer", signUpType);
        user.put("TermsAccepted", isCheckTerms);
        //user.put("admin_status", 1);
        user.put("GateCode",edGateCode.getText().toString());
        if (!state.equals("Select state")) {
            user.put("State", state);
        } else {
            user.put("State", "");
        }

        if (!userGroup.equals("Select Group")) {
            user.put("user_group", userGroup);
        } else {
            user.put("user_group", "");
        }


        user.put("PropertyNotes", edNote.getText().toString());
        if (selectedImagePath != null) {
            Object imageObject = null;
            try {
                imageObject = new BaseUtility().readInFile(selectedImagePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Create the ParseFile
            ParseFile file = new ParseFile("Image", (byte[]) imageObject);
            // Upload the image into Parse Cloud
            file.saveInBackground();
            try {
                file.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            user.put("Image", file);
        }


        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {
                    addLocation();
                    Toast.makeText(SignUpActivity.this, "Sign up Successfully", Toast.LENGTH_SHORT).show();
                    new SessionManager(SignUpActivity.this).setIsLogin();
                    sentToDashboardActivity();
                    progressDialogUtil.dismissDialog();
                } else {
                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialogUtil.dismissDialog();
                    //Register Fail
                    //get error by calling e.getMessage()
                }
            }
        });
    }

    private void addLocation() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user yet", Toast.LENGTH_SHORT).show();
        } else {
            String city = user.getString("City");
            String address = user.getString("Address");
            String zipcode = user.getString("ZipCode");
            String userState = user.getString("State");
            getLocationFromAddress(address + " " + city + "," + userState + " " + zipcode);
            ParseGeoPoint geo = new ParseGeoPoint(latitude, longitude);
            user.put("LastLocation", geo);
            user.saveInBackground();
            ParseObject object = new ParseObject("Locations");

            object.put("address", address);
            object.put("city", city);
            object.put("state", userState);
            object.put("zip", zipcode);
            object.put("Owner", user);
            object.put("GPS", geo);
            object.put("isMain", 1);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        loadLocations();
                    }
                }
            });
        }
    }

    private void getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 10);
            Address location = address.get(0);
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        } catch (Exception e) {

        }
    }

    private void loadLocations() {
        final ParseUser me = ParseUser.getCurrentUser();
        if (me != null) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Locations");
            query.whereEqualTo("Owner", me);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects != null && objects.size() > 0) {

                        } else {
                            String city = me.getString("City");
                            String address = me.getString("Address");
                            String zipcode = me.getString("ZipCode");
                            String userState = me.getString("State");
                            getLocationFromAddress(address + " " + city + "," + userState + " " + zipcode);
                            ParseGeoPoint geo = new ParseGeoPoint(latitude, longitude);
                            ParseObject object = new ParseObject("Locations");

                            object.put("address", address);
                            object.put("city", city);
                            object.put("state", state);
                            object.put("zip", zipcode);

                            object.put("Owner", me);
                            object.put("GPS", geo);
                            object.put("isMain", 1);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void sentToDashboardActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
