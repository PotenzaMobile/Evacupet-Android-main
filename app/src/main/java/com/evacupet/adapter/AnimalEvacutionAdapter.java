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
import com.evacupet.model.NotificationModel;
import com.parse.ParseObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Monil Naik on 28/03/2022.
 * MNTechnologies
 */
public class AnimalEvacutionAdapter extends RecyclerView.Adapter<AnimalEvacutionAdapter.ViewHolder> {
    private Context context;
    private ArrayList<NotificationModel> capacityModelArrayList;

    public AnimalEvacutionAdapter(Context context) {
        this.context = context;

    }

    public void setData(ArrayList<NotificationModel> capacityModelArrayList) {
        this.capacityModelArrayList = capacityModelArrayList;
        notifyDataSetChanged();

    }

    @Override
    public AnimalEvacutionAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_ev_animals, null, false);
        return new AnimalEvacutionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AnimalEvacutionAdapter.ViewHolder viewHolder, int i) {
        //Animals Object
        final ParseObject object = capacityModelArrayList.get(i).getParseObject();

        if(object!= null) {
            if(object.has("Name")) viewHolder.tv_name.setText(object.getString("Name"));
            if(object.has("HomeLocation")){
                if(object.getParseObject("HomeLocation").has("address"))
                    viewHolder.tv_location.setText(object.getParseObject("HomeLocation").getString("address"));
                if(object.getParseObject("HomeLocation").has("state") && object.getParseObject("HomeLocation").has("city"))
                    viewHolder.tv_address.setText(object.getParseObject("HomeLocation").getString("city")
                        + ",  " + object.getParseObject("HomeLocation").getString("state"));
            }
        }

        if (object.has("Status")) {
            viewHolder.iv_check_one.setEnabled(true);
            viewHolder.iv_check_two.setEnabled(true);
            if (object.getInt("Status") == 2) {
                viewHolder.iv_check_one.setEnabled(false);
                viewHolder.iv_check_two.setEnabled(false);
                viewHolder.iv_select.setSelected(true);
            }
            if (object.getInt("Status") == 3) {
                viewHolder.iv_check_one.setEnabled(false);
                viewHolder.iv_check_two.setEnabled(false);
                viewHolder.iv_select.setSelected(true);
            }
            if (object.getInt("Status") == 1){
                viewHolder.iv_select.setSelected(true);
            }
            if (object.getInt("Status") == 0){
                viewHolder.iv_select.setSelected(false);
            }
        }
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.iv_select.isSelected()){
                    viewHolder.iv_select.setSelected(false);
                }else {
                    viewHolder.iv_select.setSelected(true);
                }
                if (viewHolder.iv_select.isSelected()){
                    capacityModelArrayList.get(i).setChecked(true);

                }else {
                    capacityModelArrayList.get(i).setChecked(false);
                    object.put("Status",-1);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        int a = null != capacityModelArrayList ? capacityModelArrayList.size() : 0;
        Log.e("Adapter","Test Size=>>>"+a);
        return a;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_location)
        TextView tv_location;
        @BindView(R.id.tv_address)
        TextView tv_address;
        @BindView(R.id.iv_select)
        ImageView iv_select;
        @BindView(R.id.iv_check_one)
        TextView iv_check_one;
        @BindView(R.id.iv_check_two)
        TextView iv_check_two;
        @BindView(R.id.rl_main)
        RelativeLayout rlMain;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    public ArrayList<NotificationModel> getList(){
        return capacityModelArrayList;
    };
}
