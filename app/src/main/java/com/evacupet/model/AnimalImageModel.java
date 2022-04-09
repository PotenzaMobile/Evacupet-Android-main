package com.evacupet.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class AnimalImageModel implements Parcelable {
    private Bitmap bitmapImage;
    private String imagePath;
    private String key;
    private int num;

    public AnimalImageModel(Bitmap bitmapImage, String imagePath,String key,int num) {
        this.bitmapImage = bitmapImage;
        this.imagePath = imagePath;
        this.num = num;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Bitmap getBitmapImage() {
        return bitmapImage;
    }

    public void setBitmapImage(Bitmap bitmapImage) {
        this.bitmapImage = bitmapImage;
    }

    public AnimalImageModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.bitmapImage, flags);
    }

    protected AnimalImageModel(Parcel in) {
        this.bitmapImage = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<AnimalImageModel> CREATOR = new Creator<AnimalImageModel>() {
        @Override
        public AnimalImageModel createFromParcel(Parcel source) {
            return new AnimalImageModel(source);
        }

        @Override
        public AnimalImageModel[] newArray(int size) {
            return new AnimalImageModel[size];
        }
    };
}
