package com.evacupet.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.model.AnimalCapacityModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnimalCapacityAdapter extends RecyclerView.Adapter<AnimalCapacityAdapter.ViewHolder> {
    private Context context;
    private ArrayList<AnimalCapacityModel> capacityModelArrayList;

    public AnimalCapacityAdapter(Context context) {
        this.context = context;

    }

    public void setData(ArrayList<AnimalCapacityModel> capacityModelArrayList) {
        this.capacityModelArrayList = capacityModelArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_animal_cap, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final AnimalCapacityModel capacityModel = capacityModelArrayList.get(i);
        viewHolder.tvName.setText(capacityModel.getName());
        viewHolder.ivAnimal.setImageResource(capacityModel.getImage());
        viewHolder.edHorseCap.setText(capacityModel.getCap());
    }

    @Override
    public int getItemCount() {
        return null != capacityModelArrayList ? capacityModelArrayList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_animal)
        ImageView ivAnimal;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.ed_horse_cap)
        TextView edHorseCap;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            edHorseCap.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    capacityModelArrayList.get(getAdapterPosition()).setCap(edHorseCap.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}