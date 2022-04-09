package com.evacupet.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.evacupet.R;
import com.evacupet.fragment.AboutFragment;
import com.evacupet.fragment.NewsFragment;
import com.evacupet.utility.DialogUtility;
import com.jkb.slidemenu.SlideMenuLayout;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends DashboardActivity implements View.OnClickListener {
    @BindView(R.id.ll_news)
    LinearLayout llNews;
    @BindView(R.id.ll_about)
    LinearLayout llAbout;
    @BindView(R.id.iv_about)
    ImageView ivAbout;

    @BindView(R.id.ll_menu) LinearLayout llMenu;
    @BindView(R.id.ll_menu_left) LinearLayout llMenuLeft;

    @BindView(R.id.iv_news)
    ImageView ivNews;
    @BindView(R.id.tv_news)
    TextView tvNews;
    @BindView(R.id.tv_about)
    TextView tvAbout;

    SlideMenuLayout slideMenuLayout;

    private boolean isTermsCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //newsList = findViewById(R.id.news_list);
        //newsList.getId();

        FrameLayout contentFrameLayout = findViewById(R.id.fl_content); //Remember this is the FrameLayout area within your activity_main.xml
        titleName.setText("News");
        getLayoutInflater().inflate(R.layout.activity_home, contentFrameLayout);
        ButterKnife.bind(this);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        slideMenuLayout = findViewById(R.id.mainSlideMenu);
        init();
    }

    private void init() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();


        try{

            query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        isTermsCheck = object.getBoolean("TermsAccepted");
                        if (!isTermsCheck) {
                            new DialogUtility().termsAndConditionDialog(HomeActivity.this);
                        }
                        Log.e("terms = ", object.getBoolean("TermsAccepted") + "");
                    } else {
                        Log.e("error = ", e.getMessage());
                    }
                }
            });

        }catch (Exception e){

        }



        llAbout.setOnClickListener(this);
        llNews.setOnClickListener(this);
        llMenu.setOnClickListener(this);
        llMenuLeft.setOnClickListener(this);

        sentToNewsFragment();
        if (getIntent().hasExtra("Notification")) {
            DialogUtility.NotificationAlert(this, getIntent().getStringExtra("msg"));
        }
        sendToken();

        this.hideTopMenuButton();

    }

    private void sendToken() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (ParseUser.getCurrentUser() != null) {
            installation.put("user", ParseUser.getCurrentUser());
            installation.put("userID", ParseUser.getCurrentUser().getObjectId());
        }
        installation.saveInBackground();
    }

    @Override
    public void onImageSuccess(Intent data) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_about:
                sentToAboutFragment();
                break;
            case R.id.ll_news:
                sentToNewsFragment();
                break;


            case R.id.ll_menu:
                slideMenuLayout.openRightSlide();
                break;

            case R.id.ll_menu_left:
                slideMenuLayout.openLeftSlide();
                break;


        }
    }

    private void sentToAboutFragment() {
        ivAbout.setSelected(true);
        tvAbout.setSelected(true);
        ivNews.setSelected(false);
        tvNews.setSelected(false);
        FragmentTransaction manager = getSupportFragmentManager().beginTransaction();
        manager.replace(R.id.main_content, new AboutFragment());
        manager.commit();
    }
    private void sentToNewsFragment() {
        ivAbout.setSelected(false);
        tvAbout.setSelected(false);
        ivNews.setSelected(true);
        tvNews.setSelected(true);
        FragmentTransaction manager = getSupportFragmentManager().beginTransaction();
        manager.replace(R.id.main_content, new NewsFragment());
        manager.commit();
    }
}