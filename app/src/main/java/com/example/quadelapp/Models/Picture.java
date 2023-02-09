package com.example.quadelapp.Models;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class Picture {

    private String ID;
    private String title;
    //private String picture;
    private int image;
    private String description;
    private List<Element> detectorsList;
    private List<ControlPanel> controlPanelsList;
    private String state;
    private boolean favourite;

    public Picture(String id, String title, String description, int image, String state){
        this.ID = id;
        this.title = title;
        this.description = description;
        this.detectorsList = new ArrayList<>();
        this.controlPanelsList = new ArrayList<>();
        this.image = image;
        this.favourite = false;
        this.state = state;
    }

    public Picture (Picture p){

        this.ID = p.ID;
        this.description = p.description;
        this.detectorsList = p.detectorsList;
        this.controlPanelsList = p.controlPanelsList;
        this.image = p.image;
        this.favourite = p.favourite;
        this.state = p.state;
    }

    public Picture (String picID){

        this.ID = picID;
        this.description = "description";
        this.detectorsList = new ArrayList<>();
        this.controlPanelsList = new ArrayList<>();
        this.image = 0;
        this.favourite = false;
        this.state = "1";
    }

    public Picture (String id, String title, String description, String state){

        this.ID = id;
        this.title = title;
        this.description = description;
        this.detectorsList = new ArrayList<>();
        this.controlPanelsList = new ArrayList<>();
        this.image = 0;
        this.favourite = false;
        this.state = state;
    }

    public String getId() {
        return this.ID;
    }

    public void setId(String id) {
        this.ID = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return this.image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public List<ControlPanel> getControlPanelsList() {
        return controlPanelsList;
    }

    public List<Element> getDetectorsList() {
        return detectorsList;
    }

    public void setControlPanelsList(List<ControlPanel> controlPanelsList) {
        this.controlPanelsList = controlPanelsList;
    }

    public void setDetectorsList(List<Element> detectorsList) {
        this.detectorsList = detectorsList;
    }
    public void addControlPanelToList(ControlPanel cp){
        controlPanelsList.add(cp);
    }
    public void addDetectorToList(Element d){
        detectorsList.add(d);
    }

    public String getState(){
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isFavourite() {
        return favourite;
    }
}
