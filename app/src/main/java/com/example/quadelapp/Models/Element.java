package com.example.quadelapp.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class Element extends SystemElement {

    private String chainNumber;

    public Element(String id, String title,String description, String state, String chainNumber, String type){
        this.id = id;
        this.title = title;
        this.description = description;
        this.state = state;
        this.chainNumber = chainNumber;
        this.type = type;
    }

    public Element(String id){
        this.id = id;
        this.title = "address";
        this.description = "desc";
        this.type = "type";
        this.state = "state";
        this.chainNumber = "1";

    }

     @Override

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChainNumber() {
        return chainNumber;
    }
    public void setChainNumber(String chainNumber) {
        this.chainNumber = chainNumber;
    }
    public String getDescription() {
        return description;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
