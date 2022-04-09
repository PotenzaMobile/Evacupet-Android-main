package com.evacupet.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;


import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.adapter.AddAnimalListAdapter;
import com.evacupet.model.AnimalListModel;

import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.evacupet.utility.SwipeHelper;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnimalsActivity extends DashboardActivity implements View.OnClickListener {
    @BindView(R.id.btn_add_animal)
    Button btnAddAnimal;
    @BindView(R.id.btn_drop_animal)
    Button btnDropAnimal;
    @BindView(R.id.btn_delete_animal)
    Button btnDeleteAnimal;
    @BindView(R.id.rv_animal)
    RecyclerView rvAnimal;
    private ArrayList<AnimalListModel> animalsList;
    private AddAnimalListAdapter addAnimalListAdapter;
    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.animals));
        getLayoutInflater().inflate(R.layout.activity_animals, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        btnAddAnimal.setOnClickListener(this);
        btnDropAnimal.setOnClickListener(this);
        btnDeleteAnimal.setOnClickListener(this);
        progressDialogUtil.showDialog();
        setAnimalAdapter();
        getAllAnimals();
        enableSwipeToDeleteAndUndo();
    }

    @Override
    public void onImageSuccess(Intent data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_animal:
                Intent intent = new Intent(this, AddAnimalActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_drop_animal:
                boolean isDrop = false;
                ArrayList<AnimalListModel> modelArrayList=  new ArrayList<>();
                if (animalsList != null && !animalsList.isEmpty()) {
                    for (AnimalListModel model : animalsList) {
                        if (model.isChecked()) {
                            isDrop = true;
                            modelArrayList.add(model);
                        }
                    }
                    if (!isDrop) {
                        Toast.makeText(this, "Please select animal for drop", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent1 = new Intent(this, EvacuationCenterListActivity.class);
                        intent1.putExtra(Constant.ANIMAL_DATA, modelArrayList);
                        startActivity(intent1);
                    }
                }
                break;

            case R.id.btn_delete_animal:
                boolean isDelete = false;
                ArrayList<AnimalListModel> modelArrayListD=  new ArrayList<>();
                if (animalsList != null && !animalsList.isEmpty()) {
                    for (AnimalListModel model : animalsList) {
                        if (model.isChecked()) {
                            isDelete = true;
                            modelArrayListD.add(model);
                        }
                    }
                    if (!isDelete) {
                        Toast.makeText(this, "Please select animal to delete", Toast.LENGTH_SHORT).show();
                    } else {

                        for (AnimalListModel model : animalsList) {
                            if (model.isChecked()) {
                                try{
                                    model.getParseObject().deleteInBackground().waitForCompletion();
                                }catch (Exception e){

                                }
                            }
                        }

                        Intent intent1 = new Intent(this, AnimalsActivity.class);
                        startActivity(intent1);

                    }
                }
                break;
        }
    }

    private void setAnimalAdapter() {
        addAnimalListAdapter = new AddAnimalListAdapter(this);
        rvAnimal.setLayoutManager(new LinearLayoutManager(AnimalsActivity.this));
        rvAnimal.setAdapter(addAnimalListAdapter);
    }

    private void getAllAnimals() {
        animalsList = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Animals");
        query.whereEqualTo("Owner", ParseUser.getCurrentUser());
        query.include("EvacuatedTo");
        query.include("EvacuatedBy");
        query.include("HomeLocation");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    progressDialogUtil.dismissDialog();
                    if (objects.size() > 0) {
                        for (ParseObject object1 : objects) {
                            animalsList.add(new AnimalListModel(object1));
                        }

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                rvAnimal.setVisibility(View.VISIBLE);
                                btnDropAnimal.setVisibility(View.VISIBLE);
                                btnDeleteAnimal.setVisibility(View.VISIBLE);
                                addAnimalListAdapter.setData(animalsList);
                            }
                        });
                    } else {
                        btnDropAnimal.setVisibility(View.GONE);
                        btnDeleteAnimal.setVisibility(View.GONE);
                    }

                } else {
                    progressDialogUtil.dismissDialog();
                    btnDropAnimal.setVisibility(View.GONE);
                    btnDeleteAnimal.setVisibility(View.GONE);
                    Log.e("error = ", e.getMessage());
                }
            }
        });
    }

    private void enableSwipeToDeleteAndUndo() {

        SwipeHelper swipeHelper = new SwipeHelper(this, rvAnimal) {
            @Override
            public void instantiateUnderlayButton(final RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        0,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                final int position = viewHolder.getAdapterPosition();
                                AnimalListModel model = addAnimalListAdapter.getData().get(position);
                                ParseObject object = model.getParseObject();
                                object.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            addAnimalListAdapter.removeItem(position);
                                            if (addAnimalListAdapter.getItemCount() == 0) {
                                                btnDropAnimal.setVisibility(View.GONE);
                                                btnDeleteAnimal.setVisibility(View.GONE);
                                            }
                                            Log.e("itemCount = ", addAnimalListAdapter.getItemCount() + "");

                                        }

                                    }
                                });
                            }
                        }
                ));


            }
        };
    }
}
