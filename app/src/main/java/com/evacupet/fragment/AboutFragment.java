package com.evacupet.fragment;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.evacupet.R;
import com.evacupet.activity.SplashActivity;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AboutFragment extends Fragment {

    @BindView(R.id.aboutView) TextView aboutView;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

   private void init(){
       ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
       if (ParseUser.getCurrentUser() != null){
           parseInstallation.put("user",ParseUser.getCurrentUser());
           parseInstallation.put("userID",ParseUser.getCurrentUser().getObjectId());
           parseInstallation.saveInBackground();
       }
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           aboutView.setText(Html.fromHtml(SplashActivity.aboutData, Html.FROM_HTML_MODE_COMPACT));
       } else {
           aboutView.setText(Html.fromHtml(SplashActivity.aboutData));
       }

   }
}
