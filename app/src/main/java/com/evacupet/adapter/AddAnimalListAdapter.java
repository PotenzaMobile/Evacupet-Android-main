package com.evacupet.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evacupet.R;
import com.evacupet.activity.AddAnimalActivity;
import com.evacupet.model.AnimalListModel;
import com.evacupet.utility.Constant;
import com.parse.ParseObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddAnimalListAdapter extends RecyclerView.Adapter<AddAnimalListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<AnimalListModel> animalList;

    public AddAnimalListAdapter(Context context) {
        this.context = context;

    }

    public void setData(ArrayList<AnimalListModel> animalList) {
        this.animalList = animalList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_add_animals, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        // final ParseObject object = (ParseObject) animalList.get(i);
        final AnimalListModel model = animalList.get(i);
        final ParseObject object = model.getParseObject();
        viewHolder.tvName.setText(object.getString("Name"));
        if (object.has("EvacuatedBy")) {
            String name = object.getParseObject("EvacuatedBy").getString("FirstName") + " " + object.getParseObject("EvacuatedBy").getString("LastName");
            viewHolder.tvEvtBy.setText(name);
        }
        if (object.has("EvacuatedTo")) {
            String name = object.getParseObject("EvacuatedTo").getString("Name");
            viewHolder.tvEvtTo.setText(name);
        }
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.e("spe===", object.getString("Species"));
                sentToAddAnimalActivity(object);
            }
        });
        viewHolder.cpCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.cpCheck.isChecked()) {
                    model.setChecked(true);
                    viewHolder.cpCheck.setChecked(true);
                    Log.e("flag = ", "false");
                } else {
                    model.setChecked(false);
                    viewHolder.cpCheck.setChecked(false);
                    Log.e("flag = ", "true");
                }
            }
        });

        if (!TextUtils.isEmpty(object.getString("Species"))) {
            switch (object.getString("Species")) {
                case "Horse":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_horse);
                    break;
                case "Cow":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_cow);
                    break;
                case "Dog":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_dog);
                    break;
                case "Cat":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_cat);
                    break;
                case "Pig":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_pig);
                    break;
                case "Poultry":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_chicken);
                    break;
                case "Reptile":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_reptile);
                    break;
                case "Bird":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_bird);
                    break;
                case "Goats":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_goat);
                    break;
                case "Sheep":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_sheep);
                    break;
                case "Pocket Pet":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_pocket_pet);
                    break;
                case "Rabbit":
                    viewHolder.ivAnimal.setImageResource(R.drawable.ic_bunny);
                    break;


            }
        }


    }

    @Override
    public int getItemCount() {
        return null != animalList ? animalList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_animal)
        ImageView ivAnimal;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_evt_by)
        TextView tvEvtBy;
        @BindView(R.id.tv_evt_to)
        TextView tvEvtTo;
        @BindView(R.id.rl_main)
        RelativeLayout rlMain;
        @BindView(R.id.cp_check)
        CheckBox cpCheck;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void removeItem(int position) {
        animalList.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<AnimalListModel> getData() {
        return animalList;
    }

    private void sentToAddAnimalActivity(ParseObject object) {
        Intent intent = new Intent(context, AddAnimalActivity.class);
        intent.putExtra(Constant.ANIMAL_DATA, object);
        context.startActivity(intent);
    }

}