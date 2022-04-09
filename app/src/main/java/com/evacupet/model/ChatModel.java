package com.evacupet.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatModel implements Parcelable {
    private String messageId;
    private String createDate;
    private String comment;
    private String senderId;
    private String sender;

    public ChatModel(String comment, String senderId) {
        this.comment = comment;
        this.senderId = senderId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.messageId);
        dest.writeString(this.createDate);
        dest.writeString(this.comment);
        dest.writeString(this.senderId);
        dest.writeString(this.sender);
    }

    public ChatModel() {
    }

    protected ChatModel(Parcel in) {
        this.messageId = in.readString();
        this.createDate = in.readString();
        this.comment = in.readString();
        this.senderId = in.readString();
        this.sender = in.readString();
    }

    public static final Creator<ChatModel> CREATOR = new Creator<ChatModel>() {
        @Override
        public ChatModel createFromParcel(Parcel source) {
            return new ChatModel(source);
        }

        @Override
        public ChatModel[] newArray(int size) {
            return new ChatModel[size];
        }
    };
}
