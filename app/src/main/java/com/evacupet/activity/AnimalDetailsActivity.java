package com.evacupet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;


import com.evacupet.R;

import com.evacupet.utility.Constant;
import com.parse.ParseException;
import com.parse.ParseObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnimalDetailsActivity extends DashboardActivity implements View.OnClickListener {
    @BindView(R.id.ed_animal_name)
    TextView edAnimalName;
    @BindView(R.id.btn_done)
    Button btnDone;
    @BindView(R.id.ed_name)
    TextView edName;
    @BindView(R.id.ed_bread)
    TextView edBread;
    @BindView(R.id.ed_bday)
    TextView edBday;
    @BindView(R.id.ed_color)
    TextView edColor;
    @BindView(R.id.ed_height)
    TextView edHeight;
    @BindView(R.id.ed_tattoo)
    TextView edTattoo;
    @BindView(R.id.ed_microchip)
    TextView edMicrochip;
    @BindView(R.id.ed_behavior)
    TextView edBehavior;
    @BindView(R.id.ed_dietary)
    TextView edDietary;
    @BindView(R.id.ed_facility)
    TextView edFacility;
    @BindView(R.id.sp_trailer)
    Spinner spTrailer;
    @BindView(R.id.iv_animals_image)
    ImageView ivAnimalsImage;
    @BindView(R.id.iv_type)
    ImageView ivType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.animal_details));
        getLayoutInflater().inflate(R.layout.activity_animal_details, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setTrailerAdapter();
        setData();
        btnDone.setOnClickListener(this);
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    private void setTrailerAdapter() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, getResources().getStringArray(R.array.trailerReq));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spTrailer.setAdapter(adapter);

    }

    private void setData() {
        if (getIntent().hasExtra(Constant.ANIMAL_DATA)) {
            ParseObject object = getIntent().getParcelableExtra(Constant.ANIMAL_DATA);
            edBday.setText(object.getString("Age"));
            edBehavior.setText(object.getString("BehaviorRequirements"));
            edBread.setText(object.getString("Breed"));
            edColor.setText(object.getString("Color"));
            edDietary.setText(object.getString("DietHay"));
            edFacility.setText(object.getString("FacilityDetails"));
            edName.setText(object.getString("Name"));
            if (object.has("Height")) {
                edHeight.setText(object.getString("Height"));
            }
            if (object.has("HalterTag")) {
                edMicrochip.setText(object.getString("HalterTag"));
            }
            if (object.has("Tattoo")) {
                edTattoo.setText(object.getString("Tattoo"));
            }
            if (object.getParseFile("Image") != null) {
                byte[] profile_image_file = new byte[0];
                try {
                    profile_image_file = object.getParseFile("Image").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap profile_image_bitmap = BitmapFactory.decodeByteArray(profile_image_file, 0, profile_image_file.length);
                ivAnimalsImage.setImageBitmap(profile_image_bitmap);
                ivAnimalsImage.setVisibility(View.VISIBLE);
            }
            switch (object.getInt("Icon")) {
                case 0:
                    ivType.setBackgroundResource(R.drawable.ic_horse);
                    edAnimalName.setText("Horses & Donkeys");
                    break;
                case 1:
                    ivType.setBackgroundResource(R.drawable.ic_cow);
                    edAnimalName.setText("Cattle");

                    break;
                case 2:
                    ivType.setBackgroundResource(R.drawable.ic_sheep);
                    edAnimalName.setText("Sheep");
                    break;
                case 3:
                    ivType.setBackgroundResource(R.drawable.ic_pig);
                    edAnimalName.setText("Pigs");
                    break;
                case 4:
                    ivType.setBackgroundResource(R.drawable.ic_goat);
                    edAnimalName.setText("Goats");
                    break;
                case 5:
                    ivType.setBackgroundResource(R.drawable.ic_chicken);
                    edAnimalName.setText("Poultry");
                    break;
                case 6:
                    ivType.setBackgroundResource(R.drawable.ic_dog);
                    edAnimalName.setText("Dogs");
                    break;
                case 7:
                    ivType.setBackgroundResource(R.drawable.ic_cat);
                    edAnimalName.setText("Cats");
                    break;
                case 8:
                    ivType.setBackgroundResource(R.drawable.ic_bunny);
                    edAnimalName.setText("Bunnies & Small Animals");
                    break;
                case 9:
                    ivType.setBackgroundResource(R.drawable.ic_exotic);
                    edAnimalName.setText("Exotics");
                    break;
                default:
                    ivType.setBackgroundResource(R.drawable.ic_horse);
                    edAnimalName.setText("Horses & Donkeys");
                    break;
            }
        }
    }

    private void setState(String value) {
        for (int i = 0; i < getResources().getStringArray(R.array.trailerReq).length; i++) {
            if (value.equals(getResources().getStringArray(R.array.trailerReq)[i])) {
                spTrailer.setSelection(i);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_done:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }
}
