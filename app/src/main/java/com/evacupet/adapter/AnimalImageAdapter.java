package com.evacupet.adapter;

import android.app.Activity;
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
import com.evacupet.activity.AddAnimalActivity;
import com.evacupet.model.AnimalImageModel;
import com.evacupet.utility.Constant;
import com.evacupet.utility.ProgressDialogUtil;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnimalImageAdapter extends RecyclerView.Adapter<AnimalImageAdapter.ViewHolder> {
    private Activity context;
    private ArrayList<AnimalImageModel> animalList;
    private int flag;
    private ProgressDialogUtil progressDialogUtil;

    public AnimalImageAdapter(Activity context) {
        this.context = context;
        progressDialogUtil = new ProgressDialogUtil(context);

    }

    public void setData(ArrayList<AnimalImageModel> animalList, int flag) {
        this.animalList = animalList;
        this.flag = flag;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_image, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final AnimalImageModel model = animalList.get(i);
        viewHolder.ivAnimalImage.setImageBitmap(model.getBitmapImage());
        if (flag == 1) {
            viewHolder.ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(i);
                }
            });

        } else {


            viewHolder.ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialogUtil.showDialog();
                    switch (model.getNum()) {
                        case 1:
                            removeAndUpdateData(model.getKey(), "Image", i);
                            break;
                        case 2:
                            removeAndUpdateData(model.getKey(), "ImageOne", i);
                            break;
                        case 3:
                            removeAndUpdateData(model.getKey(), "ImageTwo", i);
                            break;
                        case 4:
                            removeAndUpdateData(model.getKey(), "ImageThree", i);
                            break;
                        case 5:
                            removeAndUpdateData(model.getKey(), "ImageFour", i);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return null != animalList ? animalList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_animal_image)
        ImageView ivAnimalImage;
        @BindView(R.id.iv_close)
        ImageView ivClose;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void removeItem(int position) {
        animalList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, animalList.size());
    }

    private void removeAndUpdateData(String objectId, final String columnName, final int position) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Animals");
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.remove(columnName);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                removeItem(position);
                            } else {
                                Log.e("error=  ", e.getMessage());
                            }
                        }
                    });

                    progressDialogUtil.dismissDialog();
                } else {
                    Log.e("error==  ", e.getMessage());
                    progressDialogUtil.dismissDialog();
                    // Failed
                }
            }
        });
    }

    public ArrayList<AnimalImageModel> getData() {
        return animalList;
    }
}