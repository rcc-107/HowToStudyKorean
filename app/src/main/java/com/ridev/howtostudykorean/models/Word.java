package com.ridev.howtostudykorean.models;

/**
 * Created by Rica on 4/28/2017.
 */

public class Word {

    private String word;
    private String meaning;
    private String commonUsages;
    private String examples;
    private String audio;

    public Word() {
    }

    public Word(String word, String meaning, String commonUsages, String examples,String audio) {
        this.word = word;
        this.meaning = meaning;
        this.commonUsages = commonUsages;
        this.examples = examples;
        this.audio = audio;
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

    public String getCommonUsages() {
        return commonUsages;
    }

    public void setCommonUsages(String commonUsages) {
        this.commonUsages = commonUsages;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
