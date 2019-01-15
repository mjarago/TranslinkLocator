package com.example.arago.translinklocator.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;

public class Bus implements Parcelable {

    private int vehicleNo;
    private int busRouteNo;
    private double busLatitude;
    private double busLongitude;
    private String busDirection;
    private String busDestination;
    private String recordedTime;



    public Bus(int vehicleNo, int busRouteNo, double busLatitude, double busLongitude, String busDirection, String busDestination, String recordedTime) {
        this.vehicleNo = vehicleNo;
        this.busRouteNo = busRouteNo;
        this.busLatitude = busLatitude;
        this.busLongitude = busLongitude;
        this.busDirection = busDirection;
        this.busDestination = busDestination;
        this.recordedTime = recordedTime;
    }

    public Bus(int busRouteNo, String busDestination){
        this.busRouteNo = busRouteNo;
        this.busDestination = busDestination;


    }

    public Bus() {
    }

    public int getVehicleNo() {
        return vehicleNo;
    }

    public String getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(String recordedTime) {
        this.recordedTime = recordedTime;
    }

    public void setVehicleNo(int vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public int getBusRouteNo() {
        return busRouteNo;
    }

    public void setBusRouteNo(int busRouteNo) {
        this.busRouteNo = busRouteNo;
    }

    public double getBusLatitude() {
        return busLatitude;
    }

    public void setBusLatitude(double busLatitude) {
        this.busLatitude = busLatitude;
    }

    public double getBusLongitude() {
        return busLongitude;
    }

    public void setBusLongitude(double busLongitude) {
        this.busLongitude = busLongitude;
    }

    public String getBusDirection() {
        return busDirection;
    }

    public void setBusDirection(String busDirection) {
        this.busDirection = busDirection;
    }

    public String getBusDestination() {
        return busDestination;
    }

    public void setBusDestination(String busDestination) {
        this.busDestination = busDestination;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}