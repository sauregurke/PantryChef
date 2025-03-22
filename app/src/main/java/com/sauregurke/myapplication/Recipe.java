package com.sauregurke.myapplication;

public class Recipe {

    private final String id;

    private final String title;

    private String linkToWebpage;


    public Recipe(String id, String title, String linkToWebpage){
        this.id = id;
        this.title = title;
        this.linkToWebpage = linkToWebpage;
    }

    public String getID(){
        return this.id;
    }

    public String getName(){
        return this.title;
    }

    public String getLink(){
        return this.linkToWebpage;
    }

    public void setLink(String link){
        this.linkToWebpage = link;
    }



}
