package com.evacupet.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.evacupet.R;
import com.evacupet.interfaceHelper.FindLocationClick;
import com.evacupet.utility.BaseUtility;
import com.evacupet.utility.DialogUtility;
import com.evacupet.utility.PassData;
import com.evacupet.utility.SessionManager;
import com.jkb.slidemenu.OnSlideChangedListener;
import com.jkb.slidemenu.SlideMenuLayout;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


abstract public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private static final int YOUR_SELECT_PICTURE_REQUEST_CODE = 101;
    private static final int ZXING_CAMERA_PERMISSION = 102;
    SlideMenuLayout slideMenuLayout;
    ImageView ivLeftMenu;
    ImageView ivRightMenu;
    TextView tvHome;
    TextView tvProfile;
    TextView tvLocation;
    TextView tvAnimals;
    TextView tvNotification;
    TextView tvReport;
    TextView tvSuggest;
    TextView tvDonate;
    TextView tvLogout;
    TextView tvCenterLocation;
    TextView tvNews;
    TextView tvEvtCenter;
    TextView tvTellYourFriends;
    TextView tvFindEvt;
    TextView titleName;
    TextView tvPrivacy;
    TextView tvEvacuation;
    LinearLayout ll_evacuation;


    private Uri outputFileUri;
    private boolean doubleBackToExitPressedOnce = false;
    private FindLocationClick findLocationClick;
    private int findLocationFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initStatusBar();
        init();
        checkCameraPermission();
    }





    public void checkCameraPermission(){


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
            }

    }




    @Override
    public void onBackPressed() {
        if (slideMenuLayout.isLeftSlideOpen() || slideMenuLayout.isRightSlideOpen()) {
            slideMenuLayout.closeLeftSlide();
            slideMenuLayout.closeRightSlide();
        } else {
            if (PassData.status) {
                DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                findLocationFlag = 2;
            } else {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    finishAffinity();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }



    private void init() {
        findLocationClick = new FindLocationClick() {
            @Override
            public void itemClick(int flag) {
                if (flag == 1) {
                    if (findLocationFlag == 1) {
                        slideMenuLayout.toggleLeftSlide();
                    } else if (findLocationFlag == 0) {
                        slideMenuLayout.toggleRightSlide();
                    } else {
                        onBackPressed();
                    }
                }
            }
        };
        slideMenuLayout = findViewById(R.id.mainSlideMenu);
        ivLeftMenu = findViewById(R.id.iv_leftMenu);
        ivRightMenu = findViewById(R.id.iv_rightMenu);
        tvHome = findViewById(R.id.tv_home);
        tvProfile = findViewById(R.id.tv_profile);
        tvLocation = findViewById(R.id.tv_location);
        tvAnimals = findViewById(R.id.tv_animals);
        tvNotification = findViewById(R.id.tv_notification);
        tvSuggest = findViewById(R.id.tv_suggest);
        tvReport = findViewById(R.id.tv_report);
        tvLogout = findViewById(R.id.tv_logout);
        tvCenterLocation = findViewById(R.id.tv_center_location);
        tvNews = findViewById(R.id.tv_news);
        tvEvtCenter = findViewById(R.id.tv_evt_center);
        tvFindEvt = findViewById(R.id.tv_find_evt);
        tvTellYourFriends = findViewById(R.id.tv_tell_your_friends);
        titleName = findViewById(R.id.title_name);
        tvDonate = findViewById(R.id.tv_donate);
        tvPrivacy = findViewById(R.id.tv_privacy);
        tvEvacuation = findViewById(R.id.tv_evt_eva);
        ll_evacuation = findViewById(R.id.ll_evacuation);

        final ParseUser pUser = ParseUser.getCurrentUser();
        if(pUser != null){
            if(pUser.getInt("admin_status")==5){
                //ll_evacuation.setVisibility(View.VISIBLE);
            }
            ll_evacuation.setVisibility(View.VISIBLE);
        }


        ivLeftMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                    findLocationFlag = 1;
                } else {
                    slideMenuLayout.toggleLeftSlide();
                }
            }
        });
        ivRightMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                    findLocationFlag = 0;
                } else {
                    slideMenuLayout.toggleRightSlide();
                }

            }
        });
        tvAnimals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.animals), AnimalsActivity.class);

                }
            }
        });

        tvNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.notification), NotificationActivity.class);

                }
            }
        });
        tvCenterLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentRightSide(getString(R.string.center_location),CenterLocationActivity.class);

                }
            }
        });
        tvEvtCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentRightSide(getString(R.string.evacuation_center),EvacuationCenterActivity.class);

                }
            }
        });

        tvEvacuation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentRightSide("Evacuations",EvacuationsActivity.class);
                }
            }
        });

        tvFindEvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentRightSide(getString(R.string.find_evacuation),FindEvacationActivity.class);

                }
            }
        });
        tvSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.suggest_a_change), SuggestActivity.class);

                }
            }
        });
        tvDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.donate), DonateActivity.class);

                }
            }
        });

        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evacu.pet/privacy/"));
                startActivity(browserIntent);
            }
        });

        tvReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentRightSide(getString(R.string.report_an_emergency),ReportEmergencyActivity.class);

                }
            }
        });
        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.profile), HomeActivity.class);

                }
            }
        });


        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.profile), ProfileActivity.class);
                }
            }
        });
        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PassData.status) {
                    DialogUtility.EvacuationProgressAlert(DashboardActivity.this, findLocationClick);
                } else {
                    sentIntentLeftSide(getString(R.string.profile), AnimalLocationActivity.class);

                }
            }
        });
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SessionManager(DashboardActivity.this).logout(getString(R.string.logout_msg));
            }
        });

        slideMenuLayout.addOnSlideChangedListener(new OnSlideChangedListener() {
            @Override
            public void onSlideChanged(SlideMenuLayout slideMenu, boolean isLeftSlideOpen, boolean isRightSlideOpen) {
                Log.d(TAG, "onSlideChanged:isLeftSlideOpen=" + isLeftSlideOpen + ":isRightSlideOpen=" + isRightSlideOpen);
                hideKeyboard(DashboardActivity.this);
            }
        });

        tvTellYourFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "EvacuPet");
                    String shareMessage = "Check it out! Evac-U-Pet is a free app that helps coordinate volunteers with those in need of assistance for their pets during emergency animal evacuations! For iOS Platform ";
                    shareMessage = shareMessage + "https://apps.apple.com/us/app/evacupet/id1420349072?ls=1"+" For Android Platform "+"https://play.google.com/store/apps/details?id=com.evacupet&hl=en";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });

    }

    public void hideTopMenuButton(){
        ivLeftMenu.setVisibility(View.INVISIBLE);
        ivRightMenu.setVisibility(View.INVISIBLE);
    }

    public void showTopMenuButton(){
        ivLeftMenu.setVisibility(View.VISIBLE);
        ivRightMenu.setVisibility(View.VISIBLE);
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        int flag_translucent_status = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setFlags(flag_translucent_status, flag_translucent_status);
    }

    private void sentIntentLeftSide(String title, Class<?> cls){
        titleName.setText(title);
        slideMenuLayout.toggleLeftSlide();
        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }
    private void sentIntentRightSide(String title, Class<?> cls){
        titleName.setText(title);
        slideMenuLayout.toggleRightSlide();
        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {
//        Log.e(TAG, "Test::lat::"+lat1);
//        Log.e(TAG, "Test::lat1::"+lat2);
//        Log.e(TAG, "Test::lon::"+lon1);
//        Log.e(TAG, "Test::lon1::"+lon2);
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static final double METERS_IN_MILE = 1609.344;

    public static double metersToMiles(double meters) {
        return meters / METERS_IN_MILE;
    }

    public static double milesToMeters(double miles) {
        return miles * METERS_IN_MILE;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e(TAG, "data = " + data.getDataString());

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {
                onImageSuccess(data);
            }
        }
    }

    abstract public void onImageSuccess(Intent data);

    public Uri imageInit() {
        outputFileUri = new BaseUtility().checkAndRequestPermissions(this);
        return outputFileUri;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        /*
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
                            new BaseUtility().showDialogOK(DashboardActivity.this, "Camera Permission required for this action",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    outputFileUri = new BaseUtility().checkAndRequestPermissions(DashboardActivity.this);
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
        */

    }

}