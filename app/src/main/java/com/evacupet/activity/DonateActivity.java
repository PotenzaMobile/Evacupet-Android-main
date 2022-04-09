package com.evacupet.activity;

import android.content.Intent;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.evacupet.R;
import com.evacupet.utility.ProgressDialogUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DonateActivity extends DashboardActivity {
    @BindView(R.id.web_view)
    WebView webView;
    ProgressDialogUtil progressDialogUtil;
    private String url = "https://www.evacu.pet/donate-2/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_donate);
        FrameLayout contentFrameLayout = findViewById(R.id.fl_content);
        getLayoutInflater().inflate(R.layout.activity_donate, contentFrameLayout);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        progressDialogUtil = new ProgressDialogUtil(this);
        titleName.setText(getString(R.string.donate));
        progressDialogUtil.showDialog();

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");


        webView.setWebViewClient(new WebViewClient(){

            /*
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(url);
                return true;
            }
            */

            @Override
            public void onPageFinished(WebView view, String url) {
                progressDialogUtil.dismissDialog();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Toast.makeText(DonateActivity.this, "Error:" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        webView.loadUrl(url);

    }

    @Override
    public void onImageSuccess(Intent data) {

    }
}
