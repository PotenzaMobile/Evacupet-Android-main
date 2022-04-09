package com.evacupet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.interfaceHelper.AddLocationClick;
import com.parse.ParseObject;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private Context context;
    private List<ParseObject> locationList;
    private AddLocationClick itemClick;
    private int flag;

    public LocationAdapter(Context context, List<ParseObject> locationList, AddLocationClick itemClick,int flag) {
        this.context = context;
        this.locationList = locationList;
        this.itemClick = itemClick;
        this.flag = flag;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_location, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final ParseObject object = locationList.get(i);
        viewHolder.tvName.setText(object.getString("address"));
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.itemClick(object,flag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return null != locationList ? locationList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.rl_main)
        RelativeLayout rlMain;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }





}