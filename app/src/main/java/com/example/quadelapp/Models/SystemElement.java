package com.example.quadelapp.Models;

public class SystemElement {

    public String id;
    protected String title;
    protected String description;
    protected String state;
    protected String type;
    //private String pictureId;

    protected int elementImage;

    public String getId(){
        return this.id;
    }
    public void setId(String Id){
        this.id = Id;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getElementImage() {
        return elementImage;
    }

    public void setElementImage(int elementImage) {
        this.elementImage = elementImage;
    }
}
