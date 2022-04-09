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
import com.evacupet.activity.AnimalDetailsActivity;
import com.evacupet.interfaceHelper.UpdateAnimalImageClick;
import com.evacupet.utility.Constant;
import com.parse.ParseObject;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoadAnimalListAdapter extends RecyclerView.Adapter<LoadAnimalListAdapter.ViewHolder> {
    private Context context;
    private List<ParseObject> animalList;
    private UpdateAnimalImageClick updateAnimalImageClick;

    public LoadAnimalListAdapter(Context context, UpdateAnimalImageClick updateAnimalImageClick) {
        this.context = context;
        this.updateAnimalImageClick = updateAnimalImageClick;
    }

    public void setData(List<ParseObject> animalList) {
        this.animalList = animalList;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_laod_animals, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( final ViewHolder viewHolder, final int i) {
        final ParseObject object = animalList.get(i);
        viewHolder.tvName.setText(object.getString("Name"));
        viewHolder.tvLocation.setText(object.getString("FacilityDetails"));

        if (object.getInt("Status") == 2) {
            viewHolder.tvAddress.setText(context.getString(R.string.loaded));
        }

        viewHolder.ivClickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnimalImageClick.itemClick(object, i, viewHolder.ivAnimalsImage);
            }
        });
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentToLoadAnimalDetailsActivity(object);
            }
        });

    }

    @Override
    public int getItemCount() {
        return null != animalList ? animalList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_animals_image)
        ImageView ivAnimalsImage;
        @BindView(R.id.iv_click_image)
        ImageView ivClickImage;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_location)
        TextView tvLocation;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.rl_main)
        RelativeLayout rlMain;

        public ViewHolder( View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void sentToLoadAnimalDetailsActivity(ParseObject object) {
        Intent intent = new Intent(context, AnimalDetailsActivity.class);
        intent.putExtra(Constant.ANIMAL_DATA, object);
        context.startActivity(intent);

    }
}