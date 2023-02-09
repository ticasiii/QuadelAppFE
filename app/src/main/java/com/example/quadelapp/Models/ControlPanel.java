package com.example.quadelapp.Models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlPanel extends SystemElement {

    private List<Element> elements;
    private Map<String, String> elementsStatesList = new HashMap<>();
    private Map<String, String> chainsStates = new HashMap<>();


    public ControlPanel(String id,String title, String desc, String type, String state){
        this.id = id;
        this.title = title;
        this.description = desc;
        this.type = type;
        this.state = state;
        this.elements = new ArrayList<Element>();
        this.chainsStates = new HashMap<String, String>();
        this.elementsStatesList = new HashMap<String, String>();
    }

    public ControlPanel(String id){
        this.id = id;
        this.title = "title";
        this.description = "desc";
        this.type = "type";
        this.state = "state";
        this.elements = new ArrayList<Element>();
        this.chainsStates = new HashMap<String, String>();
        this.elementsStatesList = new HashMap<String, String>();
    }


    //   private Map<String, Detector> chainState;//Map<chainID, detector>
    public void setId(String id) {
        id = id;
    }

    public String getId() {
        return id;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public void addElement(Element d) {
        this.elements.add(d);
    }

    public List<Element> getElements() {
        return elements;
    }

    public Map<String, String> getElementsStatesList() {
        return elementsStatesList;
    }

    public void setDetectorsStatesList(Map<String, String> detectorsStatesList) {
        this.elementsStatesList = detectorsStatesList;
    }

    public Map<String, String> getChainsStates() {
        return chainsStates;
    }

    public void setChainsStates(Map<String, String> chainsStates) {
        this.chainsStates = chainsStates;
    }

    //put -> add or update if exist
    public void putDetectorStateToDetectorsStatesList(Element element) {
        elementsStatesList.put(element.getId(), element.getState());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void changeChainState(){
        for (Element element:elements) {
            String detectorState = element.getState();
            if(detectorState == "alarm")
                chainsStates.replace(element.getChainNumber(), detectorState);
            else if (detectorState == "error" )
                this.state = "error";
            else if (detectorState == "normal")
                this.state = "normal";
        }
    }

    public void changeState() {
        for (Element detector:elements) {
            String detectorState = detector.getState();
            if(detectorState == "error")
                this.state = "error";
            else if (detectorState == "alarm" )
                this.state = "alarm";
            else if (detectorState == "normal")
                this.state = "normal";
        }
    }

}
