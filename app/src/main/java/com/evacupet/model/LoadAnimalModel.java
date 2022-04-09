package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseObject;

public class LoadAnimalModel implements Parcelable {
    private String name,facilityDetails,image;
    private ParseObject object;
    private int status;

    public LoadAnimalModel(String name, String facilityDetails, int status,ParseObject object) {
        this.name = name;
        this.facilityDetails = facilityDetails;
        this.status = status;
        this.object = object;
    }

    public String getFacilityDetails() {
        return facilityDetails;
    }

    public void setFacilityDetails(String facilityDetails) {
        this.facilityDetails = facilityDetails;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ParseObject getObject() {
        return object;
    }

    public void setObject(ParseObject object) {
        this.object = object;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public LoadAnimalModel() {
    }

    protected LoadAnimalModel(Parcel in) {
        this.name = in.readString();
    }

    public static final Creator<LoadAnimalModel> CREATOR = new Creator<LoadAnimalModel>() {
        @Override
        public LoadAnimalModel createFromParcel(Parcel source) {
            return new LoadAnimalModel(source);
        }

        @Override
        public LoadAnimalModel[] newArray(int size) {
            return new LoadAnimalModel[size];
        }
    };
}
