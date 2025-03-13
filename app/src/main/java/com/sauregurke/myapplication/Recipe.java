package com.sauregurke.myapplication;

public class Recipe {
    private String id;
    private String title;
    private String image;
    private String linkToWebpage;

    public Recipe(String id, String title, String image, String linkToWebpage){
        this.id = id;
        this.title = title;
        this.image = image;
    }

    public String getid(){
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
