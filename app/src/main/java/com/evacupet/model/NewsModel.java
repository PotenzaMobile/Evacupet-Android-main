package com.evacupet.model;

public class NewsModel {

    public int id;
    public String date;
    public String title;
    public String shortDescription;
    public String description;
    public String image;

    public NewsModel(int id, String date, String title, String shortDescription, String description, String image) {

        this.id = id;
        this.title = title;
        this.date = date;
        this.shortDescription = shortDescription;
        this.description = description;
        this.image = image;

    }

}
