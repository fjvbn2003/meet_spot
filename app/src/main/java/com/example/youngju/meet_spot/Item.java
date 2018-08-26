package com.example.youngju.meet_spot;

import android.graphics.Bitmap;

import java.net.URL;

public class Item {
    String image;
    String title;
    String desc;
    Integer num;

    int getNum(){
        return  this.num;
    }
    String getImage() {
        return this.image;
    }

    String getTitle() {
        return this.title;
    }

    String getDesc(){
        return desc;
    }

    Item(String image, String title, String desc, Integer num) {
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.num = num;
    }
}