package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

public class StateModel implements Parcelable {
    private String name;

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

    public StateModel() {
    }

    protected StateModel(Parcel in) {
        this.name = in.readString();
    }

    public static final Creator<StateModel> CREATOR = new Creator<StateModel>() {
        @Override
        public StateModel createFromParcel(Parcel source) {
            return new StateModel(source);
        }

        @Override
        public StateModel[] newArray(int size) {
            return new StateModel[size];
        }
    };
}
