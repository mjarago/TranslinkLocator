package com.example.arago.translinklocator.models;

public class UserLocation {

    private double Latitude;
    private double Longitude;

    public UserLocation(double latitude, double longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }
    public UserLocation() {
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }
}
