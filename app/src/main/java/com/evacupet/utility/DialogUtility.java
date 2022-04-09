package com.evacupet.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.evacupet.R;
import com.evacupet.activity.FindEvacationActivity;
import com.evacupet.activity.TermsAndConditionsActivity;
import com.evacupet.interfaceHelper.FindLocationClick;
import com.parse.ParseUser;


public class DialogUtility {
    public static void EvacuationProgressAlert(Context context, final FindLocationClick click) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Leaving this screen will cancel the evacuation. Are you sure that you want to do that?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PassData.status = false;
                        click.itemClick(1);

                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  PassData.status = false;
                        click.itemClick(0);
                        dialog.cancel();
                    }
                });
        AlertDialog alert1 = builder.create();
        alert1.show();

    }

    public static void NotificationAlert(final Context context, String msg) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "View",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(context, FindEvacationActivity.class);
                        context.startActivity(i);

                    }
                });

        builder.setNegativeButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });

        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    public void termsAndConditionDialog(final Context mContext) {
        // custom dialog
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogview = inflater.inflate(R.layout.dialog_term_condition, null);
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(mContext);
        dialogbuilder.setView(dialogview);
        final AlertDialog alertDialog = dialogbuilder.create();
        final Button btnYes = dialogview.findViewById(R.id.btn_yes);
        final Button btn_close = dialogview.findViewById(R.id.btn_close);
        TextView tvTermsCont = dialogview.findViewById(R.id.tv_terms_cont);

        tvTermsCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TermsAndConditionsActivity.class);
                mContext.startActivity(intent);
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SessionManager(mContext).logout(mContext.getString(R.string.app_close_msg));
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtil.isInternetOn(mContext)) {
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("TermsAccepted", true);
                    user.saveInBackground();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(mContext, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                }
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void OwnerEvacutionConfirmationDialog(final Context mContext, Intent intent) {
        // custom dialog
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialog_view = inflater.inflate(R.layout.dialog_evacution_conformation, null);
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(mContext);
        dialog_builder.setView(dialog_view);
        final AlertDialog alertDialog = dialog_builder.create();
        final Button btn_Ok = dialog_view.findViewById(R.id.btn_ok);
        final Button btn_cancel = dialog_view.findViewById(R.id.btn_cancel);

        btn_Ok.setOnClickListener(view -> mContext.startActivity(intent));
        btn_cancel.setOnClickListener (view -> alertDialog.dismiss());


        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();

    }
}
