package com.example.arago.translinklocator.models;

import java.util.ArrayList;

public class SaveArrayList {

    ArrayList<Bus> busArrayList;


    public SaveArrayList() {
    }

    public SaveArrayList(ArrayList<Bus> busArrayList) {

        this.busArrayList = busArrayList;
    }

    public ArrayList<Bus> getBusArrayList() {
        return busArrayList;
    }

    public void setBusArrayList(ArrayList<Bus> busArrayList) {
        this.busArrayList = busArrayList;
    }
}
