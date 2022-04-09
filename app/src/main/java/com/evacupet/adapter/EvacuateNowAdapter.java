package com.evacupet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.parse.ParseObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvacuateNowAdapter extends RecyclerView.Adapter<EvacuateNowAdapter.ViewHolder> {
    private Context context;
    private List<ParseObject> animalList;


    public EvacuateNowAdapter(Context context) {
        this.context = context;


    }

    public void setData(List<ParseObject> animalList){
        this.animalList = animalList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_ev_animals, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

        final ParseObject object = animalList.get(i);
        viewHolder.tvName.setText(object.getString("Name"));
        viewHolder.tvLocation.setText(object.getString("FacilityDetails"));

        if (object.has("Status")) {
           viewHolder.ivCheckOne.setEnabled(true);
           viewHolder.ivCheckTwo.setEnabled(true);
            if (object.getInt("Status") == 2) {
                viewHolder.tvAddress.setText(context.getString(R.string.loaded));
                viewHolder.ivCheckOne.setEnabled(false);
                viewHolder.ivCheckTwo.setEnabled(false);
                viewHolder.ivSelect.setSelected(true);
            }
            if (object.getInt("Status") == 3) {
                viewHolder.tvAddress.setText(context.getString(R.string.delivered));
                viewHolder.ivCheckOne.setEnabled(false);
                viewHolder.ivCheckTwo.setEnabled(false);
                viewHolder.ivSelect.setSelected(true);
            }
            if (object.getInt("Status") == 1){
                viewHolder.ivSelect.setSelected(true);
            }
            if (object.getInt("Status") == 0){
                viewHolder.ivSelect.setSelected(false);
            }
        }

        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.ivSelect.isSelected()){
                   viewHolder.ivSelect.setSelected(false);
               }else {
                   viewHolder.ivSelect.setSelected(true);
                }
               if (viewHolder.ivSelect.isSelected()){
                   object.put("Status",1);
               }else {
                   object.put("Status",-1);
               }
             }
        });
    }

    @Override
    public int getItemCount() {
        return null != animalList ? animalList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_select)
        ImageView ivSelect;
        @BindView(R.id.iv_check_one)
        TextView ivCheckOne;
        @BindView(R.id.iv_check_two)
        TextView ivCheckTwo;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_location)
        TextView tvLocation;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.rl_main)
        RelativeLayout rlMain;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}