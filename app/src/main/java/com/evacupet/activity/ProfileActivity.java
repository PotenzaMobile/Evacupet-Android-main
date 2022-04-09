package com.evacupet.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.evacupet.R;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.CompressImageUtility;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.Validation;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends DashboardActivity implements View.OnClickListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    @BindView(R.id.btn_add_user_photo)
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
    @BindView(R.id.sp_state)
    Spinner spState;
    @BindView(R.id.rg_sign_up_type)
    RadioGroup rgSignUpType;
    @BindView(R.id.btn_update)
    Button btnUpdate;
    @BindView(R.id.rb_yes)
    RadioButton rbYes;
    @BindView(R.id.rb_no)
    RadioButton rbNo;
    private String selectedImagePath;
    private Uri outputFileUri;
    boolean doubleBackToExitPressedOnce = false;
    private String state;
    private ProgressDialogUtil progressDialogUtil;
    private boolean signUpType = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.profile));
        getLayoutInflater().inflate(R.layout.activity_profile, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }
    private void init(){
        progressDialogUtil = new ProgressDialogUtil(this);
        btnUpdate.setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);
        setStateAdapter();

        //edPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        com.evacupet.utility.UsPhoneNumberFormatter addLineNumberFormatter = new com.evacupet.utility.UsPhoneNumberFormatter(
                new WeakReference<EditText>(edPhone));
        edPhone.addTextChangedListener(addLineNumberFormatter);



        rgSignUpType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_yes){
                    Log.e(TAG,"yes");
                    signUpType = true;
                }
                else if (checkedId == R.id.rb_no){
                    Log.e(TAG,"no");
                    signUpType = false;
                }
            }
        });
        setProfileData();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_update:
                attemptUpdate();
                break;
            case R.id.btn_add_user_photo:
                outputFileUri = imageInit();
                break;
        }
    }




    @Override
    public void onImageSuccess(Intent data) {
        final boolean isCamera;
        if (data.getDataString() == null) {
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
            path = new BaseUtility().getPath(this,selectedImageUri);
            //  selectedImagePath = path;
            selectedImagePath = new  CompressImageUtility().compressImage(this, path);
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
            ivUserImage.setVisibility(View.VISIBLE);
            ivUserImage.setImageBitmap(bitmap);
        }
    }

    private void attemptUpdate() {
        edUsername.setError(null);
        edEmail.setError(null);
        edPhone.setError(null);

        boolean cancel = false;
        View focusView = null;

        String username = edUsername.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String email = edEmail.getText().toString().trim();

        phone = phone.replaceAll("[^\\d]", "");


        // Check for a valid mobile address.
        if (TextUtils.isEmpty(phone)) {
            edPhone.setError(getString(R.string.error_field_required));
            focusView = edPhone;
            cancel = true;
        } else if (!Validation.isValidMobile(phone)) {
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
            if (ConnectionUtil.isInternetOn(this)) {
                updateApi();
            } else {

                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void setStateAdapter(){
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.state_list));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spState.setAdapter(adapter);
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = adapter.getItem(position);
                Log.e(TAG,state+"");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    private void updateApi(){
        progressDialogUtil.showDialog();
        ParseUser user = ParseUser.getCurrentUser();

        user.setUsername(edUsername.getText().toString());
        user.setEmail(edEmail.getText().toString());

        user.put("Address",edAddress.getText().toString());
        user.put("MobileNumber",edPhone.getText().toString());
        user.put("City",edCity.getText().toString());
        user.put("ZipCode",edZipcode.getText().toString());
        user.put("FirstName",edFirstName.getText().toString());
        user.put("LastName",edLastName.getText().toString());
        user.put("Volunteer",signUpType);
        user.put("GateCode",edGateCode.getText().toString());
        if (!state.equals("Select state")) {
            user.put("State", state);
        }
        user.put("PropertyNotes",edNote.getText().toString());
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

            user.put("Image",file);
        }

       user.saveInBackground(new SaveCallback() {
           @Override
           public void done(ParseException e) {
               progressDialogUtil.dismissDialog();

               if (e == null) {
                   Toast.makeText(ProfileActivity.this, "Update Successfully", Toast.LENGTH_SHORT).show();

               } else {
                   Log.e(TAG,"error = "+e.getMessage());
                   //Register Fail
                   //get error by calling e.getMessage()
               }

           }
       });

    }
    private void setProfileData(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            edAddress.setText(user.getString("Address"));
            edCity.setText(user.getString("City"));
            edPhone.setText(user.getString("MobileNumber"));
            edZipcode.setText(user.getString("ZipCode"));
            edFirstName.setText(user.getString("FirstName"));
            edLastName.setText(user.getString("LastName"));
            edNote.setText(user.getString("PropertyNotes"));
            edUsername.setText(user.getUsername());
            edEmail.setText(user.getEmail());
            signUpType = user.getBoolean("Volunteer");
            edGateCode.setText(user.getString("GateCode"));

            if (signUpType){
                rbYes.setChecked(true);
            }else {
                rbNo.setChecked(true);
            }
            if (!TextUtils.isEmpty(user.getString("State"))){
                state = user.getString("State").trim();
                setState(state);
            }

            if (user.getParseFile("Image") != null){
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = user.getParseFile("Image").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file , 0, profile_image_file.length);
                ivUserImage.setImageBitmap(profile_image_bitmap);
                ivUserImage.setVisibility(View.VISIBLE);
            }

        }
    }
    private void setState(String state){
        for (int i = 0; i<getResources().getStringArray(R.array.state_list).length; i++){
            if (state.equals(getResources().getStringArray(R.array.state_list)[i])){
                spState.setSelection(i);
            }
        }
    }
}