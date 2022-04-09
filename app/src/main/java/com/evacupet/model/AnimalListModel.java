package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseObject;


public class AnimalListModel implements Parcelable {
    private ParseObject parseObject;
    private boolean isChecked;


    public AnimalListModel(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.parseObject, flags);
        dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
    }

    public AnimalListModel() {
    }

    protected AnimalListModel(Parcel in) {
        this.parseObject = in.readParcelable(ParseObject.class.getClassLoader());
        this.isChecked = in.readByte() != 0;
    }

    public static final Creator<AnimalListModel> CREATOR = new Creator<AnimalListModel>() {
        @Override
        public AnimalListModel createFromParcel(Parcel source) {
            return new AnimalListModel(source);
        }

        @Override
        public AnimalListModel[] newArray(int size) {
            return new AnimalListModel[size];
        }
    };
}
