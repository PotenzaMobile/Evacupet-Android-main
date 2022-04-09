package com.evacupet.activity;

import android.os.Bundle;

import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.evacupet.R;


import butterknife.BindView;
import butterknife.ButterKnife;

public class TermsAndConditionsActivity extends AppCompatActivity {
    @BindView(R.id.tv_terms_cont)
    WebView tvTermsCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        tvTermsCont.loadUrl("file:///android_asset/terms_cont.html");

      /*  try {
            InputStream is = getAssets().open("terms_cont.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);

            // Finally stick the string into the text view.
            tvTermsCont.setText(Html.fromHtml(Html.fromHtml(text).toString()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}
