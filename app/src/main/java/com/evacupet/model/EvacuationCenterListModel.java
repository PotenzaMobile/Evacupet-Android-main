package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.evacupet.utility.Constant;
import com.google.gson.annotations.SerializedName;

public class EvacuationCenterListModel implements Parcelable {
    @SerializedName(Constant.CENTER)
    private String center;
    @SerializedName(Constant.NAME)
    private String name;
    @SerializedName(Constant.ADDRESS)
    private String address;
    @SerializedName(Constant.STATE)
    private String state;
    @SerializedName(Constant.ZIP_CODE)
    private String zipCode;
    @SerializedName(Constant.CITY)
    private String city;
    @SerializedName(Constant.CONTACT)
    private String contact;
    @SerializedName(Constant.LAT)
    private String lat;
    @SerializedName(Constant.LONG)
    private String centerLong;

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getCenterLong() {
        return centerLong;
    }

    public void setCenterLong(String centerLong) {
        this.centerLong = centerLong;
    }

    public EvacuationCenterListModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.center);
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.state);
        dest.writeString(this.zipCode);
        dest.writeString(this.city);
        dest.writeString(this.contact);
        dest.writeString(this.lat);
        dest.writeString(this.centerLong);
    }

    protected EvacuationCenterListModel(Parcel in) {
        this.center = in.readString();
        this.name = in.readString();
        this.address = in.readString();
        this.state = in.readString();
        this.zipCode = in.readString();
        this.city = in.readString();
        this.contact = in.readString();
        this.lat = in.readString();
        this.centerLong = in.readString();
    }

    public static final Creator<EvacuationCenterListModel> CREATOR = new Creator<EvacuationCenterListModel>() {
        @Override
        public EvacuationCenterListModel createFromParcel(Parcel source) {
            return new EvacuationCenterListModel(source);
        }

        @Override
        public EvacuationCenterListModel[] newArray(int size) {
            return new EvacuationCenterListModel[size];
        }
    };
}
