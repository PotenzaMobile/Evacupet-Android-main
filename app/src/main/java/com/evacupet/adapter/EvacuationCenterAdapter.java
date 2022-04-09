package com.evacupet.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.activity.ViewLocationActivity;
import com.evacupet.interfaceHelper.AddLocationClick;
import com.evacupet.interfaceHelper.EvacuationCenterClick;
import com.evacupet.model.EvacuationCenterListModel;
import com.evacupet.utility.Constant;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvacuationCenterAdapter extends RecyclerView.Adapter<EvacuationCenterAdapter.ViewHolder> {
    private Context context;
    private ArrayList<EvacuationCenterListModel> centerLocationList;
    private EvacuationCenterClick locationClick;

    public EvacuationCenterAdapter(Context context, EvacuationCenterClick locationClick) {
        this.context = context;
        this.locationClick = locationClick;

    }

    public void setData(ArrayList<EvacuationCenterListModel> centerLocationList){
        this.centerLocationList = centerLocationList;
    }



    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_center_location, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder viewHolder, int i) {
        final EvacuationCenterListModel object =  centerLocationList.get(i);
        viewHolder.tvDes.setVisibility(View.GONE);
        viewHolder.tvName.setText(object.getName());
        viewHolder.tvAddress.setText(String.format("%s,%s,%s,%s", object.getAddress(), object.getCity(), object.getState(), object.getZipCode()));
        viewHolder.tvDes.setText("Accepting All Animals");
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("lattt  =",object.getLat()+"gfhghjghg = "+object.getCenterLong());
                locationClick.itemClick(object.getCenter(),object.getLat(),object.getCenterLong());
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

        public ViewHolder( View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}