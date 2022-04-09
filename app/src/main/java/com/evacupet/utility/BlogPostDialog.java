package com.evacupet.utility;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.evacupet.R;
import com.evacupet.model.NewsModel;
import com.squareup.picasso.Picasso;

public class BlogPostDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;

    TextView title;
    TextView description;
    TextView dateTime;
    ImageView image;

    NewsModel newsModel;


    public BlogPostDialog(Activity a, NewsModel news) {
        super(a,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.newsModel = news;
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.blog_post);

        title = (TextView) findViewById(R.id.title);
        description = (TextView) findViewById(R.id.content);
        dateTime = (TextView) findViewById(R.id.date);
        image = (ImageView) findViewById(R.id.image);

        title.setText(newsModel.title);
        dateTime.setText(newsModel.date);
        //description.setText(newsModel.description);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            description.setText(Html.fromHtml(newsModel.description, Html.FROM_HTML_MODE_COMPACT));
        } else {
            description.setText(Html.fromHtml(newsModel.description));
        }

        try {
            Picasso.get().load(newsModel.image).into(image);
        }catch (Exception e){
        }
        getWindow().getAttributes().windowAnimations = R.style.Animation_Design_BottomSheetDialog;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                c.finish();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}