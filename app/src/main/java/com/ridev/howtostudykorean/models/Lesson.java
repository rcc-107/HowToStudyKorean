package com.ridev.howtostudykorean.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rica on 4/20/2017.
 */

public class Lesson implements Parcelable {

    private String title;
    private String desc;
    private ArrayList<HashMap<String,String>> text = new ArrayList<HashMap<String, String>>();

    public Lesson() {
    }

    public Lesson(String desc, ArrayList<HashMap<String,String>> text,String title) {
        this.desc = desc;
        this.text = text;
        this.title = title;
    }

    protected Lesson(Parcel in) {
        title = in.readString();
        desc = in.readString();
        text = (ArrayList<HashMap<String, String>>) in.readSerializable();
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeSerializable(text);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ArrayList<HashMap<String,String>> getText() {
        return text;
    }

    public void setText(ArrayList<HashMap<String,String>> text) {
        this.text = text;
    }

}
