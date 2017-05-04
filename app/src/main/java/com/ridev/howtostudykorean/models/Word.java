package com.ridev.howtostudykorean.models;

/**
 * Created by Rica on 4/28/2017.
 */

public class Word {

    private String word;
    private String meaning;
    private String comUsages;
    private String examples;

    public Word() {
    }

    public Word(String word, String meaning, String comUsages, String examples) {
        this.word = word;
        this.meaning = meaning;
        this.comUsages = comUsages;
        this.examples = examples;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getComUsages() {
        return comUsages;
    }

    public void setComUsages(String comUsages) {
        this.comUsages = comUsages;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }
}
