package com.example.arago.translinklocator.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BusStop implements Parcelable{

     private int stopNo;
     private String stopName;
     private double stopLatitude;
     private double stopLongitude;
     private double stopDistance;
     private String stopRoutes;

    public BusStop(int stopNo, String stopName, double stopLatitude, double stopLongitude, double stopDistance, String stopRoutes) {
        this.stopNo = stopNo;
        this.stopName = stopName;
        this.stopLatitude = stopLatitude;
        this.stopLongitude = stopLongitude;
        this.stopDistance = stopDistance;
        this.stopRoutes = stopRoutes;
    }

    public BusStop() {
    }

    public int getStopNo() {
        return stopNo;
    }

    public void setStopNo(int stopNo) {
        this.stopNo = stopNo;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public double getStopLatitude() {
        return stopLatitude;
    }

    public void setStopLatitude(double stopLatitude) {
        this.stopLatitude = stopLatitude;
    }

    public double getStopLongitude() {
        return stopLongitude;
    }

    public void setStopLongitude(double stopLongitude) {
        this.stopLongitude = stopLongitude;
    }

    public double getStopDistance() {
        return stopDistance;
    }

    public void setStopDistance(double stopDistance) {
        this.stopDistance = stopDistance;
    }

    public String getStopRoutes() {
        return stopRoutes;
    }

    public void setStopRoutes(String stopRoutes) {
        this.stopRoutes = stopRoutes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
