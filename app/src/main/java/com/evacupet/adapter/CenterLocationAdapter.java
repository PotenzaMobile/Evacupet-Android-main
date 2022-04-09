package com.evacupet.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.activity.EvacuationCenterActivity;
import com.evacupet.activity.ViewLocationActivity;
import com.evacupet.utility.Constant;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CenterLocationAdapter extends RecyclerView.Adapter<CenterLocationAdapter.ViewHolder> {
    private Context context;
    private List<ParseObject> centerLocationList;

    public CenterLocationAdapter(Context context) {
        this.context = context;

    }

    public void setData(List<ParseObject> centerLocationList){
        this.centerLocationList = centerLocationList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_center_location, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final ParseObject object = (ParseObject) centerLocationList.get(i);
        viewHolder.tvName.setText(object.getString("Name"));
        viewHolder.tvAddress.setText(String.format("%s,%s,%s,%s", object.getString("Address"), object.getString("City"), object.getString("State"), object.getString("ZipCode")));
        viewHolder.tvDes.setText("Accepting All Animals");
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Intent intent = new Intent(context, ViewLocationActivity.class);
               Intent intent = new Intent(context, EvacuationCenterActivity.class);
               intent.putExtra(Constant.VIEW_LOCATION,object);
               context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return null != centerLocationList ? centerLocationList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_animals_image)
        ImageView ivAnimal;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.tv_des)
        TextView tvDes;
        @BindView(R.id.rl_main)
        RelativeLayout rlMain;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}