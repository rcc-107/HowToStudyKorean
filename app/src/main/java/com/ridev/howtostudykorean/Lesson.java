package com.ridev.howtostudykorean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rica on 4/20/2017.
 */

public class Lesson {

    private String title;
    private String desc;
    private ArrayList<HashMap<String,String>> text;
    private ArrayList<String> nouns;
    private ArrayList<String> verbs;
    private ArrayList<String> otherWords;

    public Lesson() {
    }

    public Lesson(String desc, ArrayList<HashMap<String,String>> text, ArrayList<String> nouns, ArrayList<String> verbs, ArrayList<String> otherWords) {
        this.desc = desc;
        this.text = text;
        this.nouns = nouns;
        this.verbs = verbs;
        this.otherWords = otherWords;
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

    public ArrayList<String> getNouns() {
        return nouns;
    }

    public void setNouns(ArrayList<String> nouns) {
        this.nouns = nouns;
    }

    public ArrayList<String> getVerbs() {
        return verbs;
    }

    public void setVerbs(ArrayList<String> verbs) {
        this.verbs = verbs;
    }

    public ArrayList<String> getOtherWords() {
        return otherWords;
    }

    public void setOtherWords(ArrayList<String> otherWords) {
        this.otherWords = otherWords;
    }
}
