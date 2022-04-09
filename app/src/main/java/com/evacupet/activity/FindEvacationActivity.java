package com.evacupet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evacupet.R;
import com.evacupet.utility.ConnectionUtil;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FindEvacationActivity extends DashboardActivity implements View.OnClickListener {
    @BindView(R.id.btn_yes)
    Button btnYes;
    @BindView(R.id.btn_no)
    Button btnNo;
    @BindView(R.id.ll_button)
    LinearLayout llButton;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rl_assist_eva)
    LinearLayout rlAssistEva;
    @BindView(R.id.sp_trailer)
    Spinner spTrailer;
    @BindView(R.id.ed_capacity)
    EditText edCapacity;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    @BindView(R.id.rl_weight)
    RelativeLayout rlWeight;
    @BindView(R.id.rl_trailer_type)
    RelativeLayout rlTrailerType;
    @BindView(R.id.sp_animal_type)
    Spinner spAnimalType;
    @BindView(R.id.sp_weight)
    Spinner spWeight;
    private String trailerType = "", animalType, weight = "";
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.find_evacuation));
        getLayoutInflater().inflate(R.layout.activity_find_evacation, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnNo.setOnClickListener(this);
        btnYes.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        setSpinnerAdapter();
        setWeightAdapter();
        if (getIntent().hasExtra(Constant.NOTIFICATION_FLAG)) {
            rlAssistEva.setVisibility(View.VISIBLE);
            llButton.setVisibility(View.GONE);
            tvTitle.setText("Please let us know about your available vehicle");
        }

    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_no:
                Intent intent = new Intent(FindEvacationActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.btn_yes:
                rlAssistEva.setVisibility(View.VISIBLE);
                llButton.setVisibility(View.GONE);
                tvTitle.setText("Please let us know about your available vehicle");
                break;

            case R.id.btn_submit:
                if (ConnectionUtil.isInternetOn(this)) {
                    evacuationProgram();
                } else {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void setSpinnerAdapter() {
        final ArrayAdapter<String> animalTypeAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.animalType));
        animalTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spAnimalType.setAdapter(animalTypeAdapter);
        spAnimalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                animalType = animalTypeAdapter.getItem(position);
                if (animalType.equals("Horse")) {
                    setTrailerSpinnerAdapter(getResources().getStringArray(R.array.hoursTrailerType));
                    rlWeight.setVisibility(View.GONE);
                    rlTrailerType.setVisibility(View.VISIBLE);
                } else if (animalType.equals("Kennel")) {
                    rlWeight.setVisibility(View.VISIBLE);
                    rlTrailerType.setVisibility(View.GONE);
                } else if (animalType.equals("Other")) {
                    setTrailerSpinnerAdapter(getResources().getStringArray(R.array.otherTrailerType));
                    rlWeight.setVisibility(View.GONE);
                    rlTrailerType.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void setTrailerSpinnerAdapter(String[] list) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, list);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spTrailer.setAdapter(adapter);
        spTrailer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trailerType = adapter.getItem(position);
                if (trailerType.equals("Trailer Type")) {
                    trailerType = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setWeightAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.weight));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spWeight.setAdapter(adapter);
        spWeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weight = adapter.getItem(position);
                if (weight.equals("Select Weight")) {
                    weight = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void evacuationProgram() {
        ParseUser user = ParseUser.getCurrentUser();
        String edCap = edCapacity.getText().toString();
        if (TextUtils.isEmpty(edCap)) {
            edCapacity.setError(getString(R.string.error_field_required));
        } else if (animalType.equals("Animal Type")) {
            Toast.makeText(this, "Select Animal Type", Toast.LENGTH_SHORT).show();
        } else if (trailerType.equals("")  && animalType.equals("Horse")) {
            Toast.makeText(this, "Select Trailer Type", Toast.LENGTH_SHORT).show();
        } else {
            progressDialogUtil.showDialog();
            Log.e("Capacity = ", edCap);
            user.put("Capacity", edCap);
            user.put("TrailerType", trailerType);
            user.put("weight", weight);
            user.put("animal_type", animalType);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    progressDialogUtil.dismissDialog();
                    if (e == null) {
                        Intent intent = new Intent(FindEvacationActivity.this, EvacuationProgressActivity.class);
                        startActivity(intent);
                    } else {
                        Log.e("error = ", e.getMessage());
                    }
                }
            });
        }
    }
}