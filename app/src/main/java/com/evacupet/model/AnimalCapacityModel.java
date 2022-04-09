package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AnimalCapacityModel implements Parcelable {
    private String name, cap, key;
    private int image;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AnimalCapacityModel(String name, String key, int image) {
        this.name = name;
        this.key = key;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
    }

    public AnimalCapacityModel() {
    }

    protected AnimalCapacityModel(Parcel in) {
        this.name = in.readString();
    }

    public static final Creator<AnimalCapacityModel> CREATOR = new Creator<AnimalCapacityModel>() {
        @Override
        public AnimalCapacityModel createFromParcel(Parcel source) {
            return new AnimalCapacityModel(source);
        }

        @Override
        public AnimalCapacityModel[] newArray(int size) {
            return new AnimalCapacityModel[size];
        }
    };
}
