package com.evacupet.utility;


import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.evacupet.R;

public class ProgressDialogUtil extends DialogFragment {
    AlertDialog alertDialog;
    AlertDialog.Builder builder;

    public ProgressDialogUtil(Activity context) {
        builder = new AlertDialog.Builder(context);


        LayoutInflater inflater = context.getLayoutInflater();
        alertDialog =builder.setView(inflater.inflate(R.layout.progressbar,null)).create();
    }


    public void showDialog() {
        if(builder!= null) alertDialog.show();
    }

    public void dismissDialog(){
        if(builder!= null) alertDialog.dismiss();
    }
}