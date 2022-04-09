package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseObject;

/**
 * Created by Monil Naik on 28/03/2022.
 * MNTechnologies
 */
public class NotificationModel implements Parcelable {
    private ParseObject parseObject;
    private boolean isChecked;
    private String status = "";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NotificationModel(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    protected NotificationModel(Parcel in) {
        this.parseObject = in.readParcelable(ParseObject.class.getClassLoader());
        this.isChecked = in.readByte() != 0;
    }

    public ParseObject getParseObject() {return parseObject;}

    public void setParseObject(ParseObject parseObject) {this.parseObject = parseObject;}

    public static final Creator<NotificationModel> CREATOR = new Creator<NotificationModel>() {
        @Override
        public NotificationModel createFromParcel(Parcel in) {
            return new NotificationModel(in);
        }

        @Override
        public NotificationModel[] newArray(int size) {
            return new NotificationModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.parseObject, flags);
        parcel.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
    }


    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
