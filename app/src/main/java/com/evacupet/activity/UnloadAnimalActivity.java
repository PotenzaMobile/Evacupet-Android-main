package com.evacupet.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.evacupet.R;
import com.evacupet.adapter.LoadAnimalListAdapter;
import com.evacupet.interfaceHelper.UpdateAnimalImageClick;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.CompressImageUtility;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

public class UnloadAnimalActivity extends DashboardActivity implements View.OnClickListener, UpdateAnimalImageClick {
    private static final String TAG = UnloadAnimalActivity.class.getSimpleName();
    private final int REQUEST_CODE_CLICK_IMAGE = 102;
    private final int REQUEST_ACCESS_MEDIA_PERMISSION = 104;
    private static final int ZXING_CAMERA_PERMISSION = 102;
    @BindView(R.id.rv_unload_animal)
    RecyclerView rvUnloadAnimal;
    @BindView(R.id.btn_unload_animal)
    Button btnUnloadAnimal;
    private ProgressDialogUtil progressDialogUtil;
    private String selectedImagePath;
    private Uri outputFileUri;
    private ImageView animalImage;
    private List<ParseObject> animalList;
    private int imagePosition;
    private ParseObject parseObject;
    private LoadAnimalListAdapter animalListAdapter;
    private String evtCenterName = "";
    private int catCapacity = 0, horseCapacity = 0, cowCapacity = 0, sheepCapacity = 0, pigCapacity = 0, goatCapacity = 0, poultryCapacity = 0, dogCapacity = 0, reptileCapacity = 0, birdCapacity = 0, pocketPetCapacity = 0, rabbitCapacity = 0, otherCapacity = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText(getString(R.string.unload_animals));
        getLayoutInflater().inflate(R.layout.activity_unload_animal, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        progressDialogUtil.showDialog();
        if (getIntent().hasExtra(Constant.GO_CENTER)) {
            parseObject = getIntent().getParcelableExtra(Constant.GO_CENTER);
            if (getIntent().hasExtra(Constant.EVT_CENTER_NAME)) {
                evtCenterName = getIntent().getStringExtra(Constant.EVT_CENTER_NAME);
            }
        }
        setAdapter();
        getAnimalsData();
        btnUnloadAnimal.setOnClickListener(this);
    }

    @Override
    public void onImageSuccess(Intent data) {
    }

    private void getAnimalsData() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Animals");
        query.whereEqualTo("Status", 2);
        query.whereEqualTo("EvacuatedBy", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null) {
                    for (ParseObject object : objects) {
                        try {
                            Log.e(TAG, new BaseUtility().parseObjectToJson(object) + "");
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                    animalList = objects;
                    animalListAdapter.setData(objects);
                    animalListAdapter.notifyDataSetChanged();
                    progressDialogUtil.dismissDialog();

                } else {
                    progressDialogUtil.dismissDialog();
                    Log.e("animal error = ", e.getMessage());
                }

            }
        });
    }

    private void setAdapter() {
        animalListAdapter = new LoadAnimalListAdapter(this, this);
        rvUnloadAnimal.setLayoutManager(new LinearLayoutManager(UnloadAnimalActivity.this));
        rvUnloadAnimal.setAdapter(animalListAdapter);
    }

    private void doneUnloadAnimal(String dropOffNote) {
        // progressDialogUtil.showDialog();
        if (animalList != null && animalList.size() > 0) {
            for (final ParseObject object : animalList) {
                if (object.getInt("Status") == 3) {
                    progressDialogUtil.showDialog();
                    object.put("EvacuatedBy", ParseUser.getCurrentUser());
                    object.put("Status", 3);
                    object.put("EvacuatedTo", parseObject);
                    try {
                        if (object.getParseFile("PickupPic") != null) {
                            object.put("DropoffPic", object.getParseFile("PickupPic"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    parseObject.put("DropOffNotes", dropOffNote);
                    parseObject.saveInBackground();
                    object.saveInBackground();
                    new BaseUtility().sendNotification(object.getParseObject("Owner").getObjectId(), "Your animals have been evacuated to" + evtCenterName);
                }
            }
            ParseUser parseUser = ParseUser.getCurrentUser();
            parseUser.put("CapacityUsed", 0);
            parseUser.saveInBackground();
            Intent intent = new Intent(UnloadAnimalActivity.this, FindEvacationActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_unload_animal:
                dialogDropOffDialog();
                break;
        }
    }

    @Override
    public void itemClick(ParseObject data, int position, ImageView imageView) {
        animalImage = imageView;
        imagePosition = position;
        checkAndRequestPermissions();
    }

    public Uri checkAndRequestPermissions() {
        Uri outputImage = null;
        int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), ZXING_CAMERA_PERMISSION);
            return outputImage;
        } else {
            openCameraIntent();
        }
        return outputImage;
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

                        openCameraIntent();

                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            new BaseUtility().showDialogOK(UnloadAnimalActivity.this, "Camera Permission required for this action",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
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

    private void openCameraIntent() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(getExternalCacheDir(), "cropped.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, getString(R.string.file_provider_authority), file));
            startActivityForResult(intent, REQUEST_CODE_CLICK_IMAGE);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask), REQUEST_ACCESS_MEDIA_PERMISSION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void setPickupImage() {
        if (selectedImagePath != null) {
            Object imageObject = null;
            try {
                imageObject = new BaseUtility().readInFile(selectedImagePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Create the ParseFile
            ParseFile file = new ParseFile("Image.png", (byte[]) imageObject);
            // Upload the image into Parse Cloud
            file.saveInBackground();
            try {
                file.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            animalList.get(imagePosition).put("PickupPic", file);
            animalList.get(imagePosition).put("Status", 3);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CLICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImageUri = Uri.fromFile(new File(getExternalCacheDir(), "cropped.jpg"));
            selectedImagePath = new CompressImageUtility().compressImage(this, selectedImageUri.getPath());
            Log.e(TAG, selectedImagePath + "...");
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
            setPickupImage();
            animalImage.setImageBitmap(bitmap);
        }
    }

    public void dialogDropOffDialog() {
        boolean checkPic = false;
        if (animalList != null && animalList.size() > 0) {
            for (final ParseObject object : animalList) {
                if (object.getInt("Status") != 3) {
                    checkPic = true;
                }
            }
        }
        if (!checkPic) {
            // custom dialog
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogview = inflater.inflate(R.layout.dialog_load_description, null);
            AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
            dialogbuilder.setView(dialogview);
            final AlertDialog alertDialog = dialogbuilder.create();
            final Button btnYes = dialogview.findViewById(R.id.btn_yes);
            final Button btn_close = dialogview.findViewById(R.id.btn_close);
            final EditText edDropOffNote = dialogview.findViewById(R.id.ed_drop_off_note);


            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(edDropOffNote.getText().toString())) {
                        // doneUnloadAnimal(edDropOffNote.getText().toString());
                        checkCapacity(edDropOffNote.getText().toString());
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(UnloadAnimalActivity.this, "Please add drop off note!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        } else {
            Toast.makeText(this, "Please capture animal image", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCapacity(String note) {
        catCapacity = 0;
        horseCapacity = 0;
        cowCapacity = 0;
        sheepCapacity = 0;
        pigCapacity = 0;
        goatCapacity = 0;
        poultryCapacity = 0;
        dogCapacity = 0;
        reptileCapacity = 0;
        birdCapacity = 0;
        pocketPetCapacity = 0;
        rabbitCapacity = 0;
        otherCapacity = 0;
        if (animalList != null && animalList.size() > 0) {
            for (final ParseObject object : animalList) {
                String species = object.getString("Species");
                if (!TextUtils.isEmpty(species)) {
                    switch (species) {
                        case Constant.HORSE:
                            horseCapacity++;
                            break;
                        case Constant.BIRD:
                            birdCapacity++;
                            break;
                        case Constant.DOG:
                            dogCapacity++;
                            break;
                        case Constant.CAT:
                            catCapacity++;
                            break;
                        case Constant.REPTILE:
                            reptileCapacity++;
                            break;
                        case Constant.GOATS:
                            goatCapacity++;
                            break;
                        case Constant.SHEEP:
                            sheepCapacity++;
                            break;
                        case Constant.POCKET_PET:
                            pocketPetCapacity++;
                            break;
                        case Constant.RABBIT:
                            rabbitCapacity++;
                            break;
                        case Constant.OTHER:
                            otherCapacity++;
                            break;
                        case Constant.COW:
                            cowCapacity++;
                            break;
                        case Constant.PIG:
                            pigCapacity++;
                            break;
                        case Constant.POULTRY:
                            poultryCapacity++;
                            break;
                    }
                    checkCapacityApi(note);

                }

            }
        }


    }

    private void checkCapacityApi(final String note) {
        String otherFlag = "0";
        if (otherCapacity != 0) {
            otherFlag = "1";
        }
        progressDialogUtil.showDialog();
        AndroidNetworking.post("https://evacu.pet/alert-now/checkcapicatyOfAnimal.php")
                .addBodyParameter("center_id", parseObject.getObjectId())
                .addBodyParameter("CatCapacity", String.valueOf(catCapacity))
                .addBodyParameter("PigCapacity", String.valueOf(pigCapacity))
                .addBodyParameter("CowCapacity", String.valueOf(cowCapacity))
                .addBodyParameter("HorseCapacity", String.valueOf(horseCapacity))
                .addBodyParameter("GoatCapacity", String.valueOf(goatCapacity))
                .addBodyParameter("DogCapacity", String.valueOf(dogCapacity))
                .addBodyParameter("BunnyCapacity", String.valueOf(rabbitCapacity))
                .addBodyParameter("ChickenCapacity", String.valueOf(poultryCapacity))
                .addBodyParameter("SheepCapacity", String.valueOf(sheepCapacity))
                .addBodyParameter("Other", String.valueOf(otherCapacity))
                .addBodyParameter("Reptile", String.valueOf(reptileCapacity))
                .addBodyParameter("Bird", String.valueOf(birdCapacity))
                .addBodyParameter("Pocketpet", String.valueOf(pocketPetCapacity))
                .addBodyParameter("flag", otherFlag)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialogUtil.dismissDialog();
                        Log.e(TAG, "check  = " + response.toString());
                        String status = response.optString("status");
                        if (status.equals("Fail")) {
                            String remCapacity = response.optString("Remmmaning Capacity");
                            String pet = response.optString("pet");
                            Toast.makeText(UnloadAnimalActivity.this, "You Cant't unload more than " + remCapacity + " " + pet, Toast.LENGTH_SHORT).show();
                        } else {
                            doneUnloadAnimal(note);
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