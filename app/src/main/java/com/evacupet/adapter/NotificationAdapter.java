package com.evacupet.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.activity.AnimalEvacutionActivity;
import com.evacupet.activity.AnimalLocationActivity;
import com.evacupet.activity.HomeActivity;
import com.evacupet.model.NotificationModel;
import com.evacupet.utility.Constant;
import com.evacupet.utility.DialogUtility;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Monil Naik on 28/03/2022.
 * MNTechnologies
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private ArrayList<NotificationModel> capacityModelArrayList;

    public NotificationAdapter(Context context) {
        this.context = context;

    }

    public void setData(ArrayList<NotificationModel> capacityModelArrayList) {
        this.capacityModelArrayList = capacityModelArrayList;
        notifyDataSetChanged();

    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_notification, null, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationAdapter.ViewHolder viewHolder, int i) {
        viewHolder.ll_item.setOnClickListener(view -> {
            Intent intent = new Intent(context, AnimalEvacutionActivity.class);
            intent.putExtra(Constant.NOTIFICATION_OBJECT,capacityModelArrayList.get(i).getParseObject());
            new DialogUtility().OwnerEvacutionConfirmationDialog(context,intent);

        });
        final ParseObject capacityModel = capacityModelArrayList.get(i).getParseObject();
        if (capacityModel.has("Event")) {
            String name = "",description="",status="";
            status = capacityModelArrayList.get(i).getStatus();
            try {
                name = capacityModel.getParseObject("Event").fetchIfNeeded().getString("title");
                description = capacityModel.getParseObject("Event").fetchIfNeeded().getString("description");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            viewHolder.tv_event_name.setText(name);
            viewHolder.tv_event_des.setText(description);
            viewHolder.tv_status.setText(status);
        }
        else{Log.e("Adapter","Test=>>>Not Found"+i);}
    }

    @Override
    public int getItemCount() {
        int a = null != capacityModelArrayList ? capacityModelArrayList.size() : 0;
        Log.e("Adapter","Test Size=>>>"+a);
        return a;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_event_name)
        TextView tv_event_name;
        @BindView(R.id.tv_event_des)
        TextView tv_event_des;
        @BindView(R.id.tv_status)
        TextView tv_status;
        @BindView(R.id.ll_item)
        LinearLayoutCompat ll_item;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}