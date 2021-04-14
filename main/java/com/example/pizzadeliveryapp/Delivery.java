package com.example.pizzadeliveryapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class Delivery
{
    //Attributes
    private int deliveryID;
    private int sessionID;
    private LocalTime startTime;
    private LocalTime deliveredTime;
    private String duration;
    private String address;
    private String distance;

    //Getters and Setters
    public int getDeliveryID() {return deliveryID;}
    public void setDeliveryID(int sessionID) {this.deliveryID = deliveryID;}

    public int getSessionID() {return sessionID;}
    public void setSessionID(int sessionID) {this.sessionID = sessionID;}

    public LocalTime getStartTime() {return startTime;}
    public void setStartTime(LocalTime startTime) {this.startTime = startTime;}

    public LocalTime getDeliveredTime() {return deliveredTime;}
    public void setDeliveredTime(LocalTime deliveredTime) {this.deliveredTime = deliveredTime;}

    public String getDuration() {return duration;}
    public void setDuration(String duration) {this.duration = duration;}

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public String getDistance() {return distance;}
    public void setDistance(String distance) {this.distance = distance;}


    //Constructors
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Delivery()
    {
        deliveryID = 0;
        sessionID = 0;
        startTime = LocalTime.now();
        deliveredTime = LocalTime.now();
        duration = "";
        address = "";
        distance = "";
    }

    public Delivery(int deliveryIn, int sessionIn, LocalTime startIn, LocalTime deliveredIn, String durationIn, String addressIn, String distanceIn)
    {
        deliveryID = deliveryIn;
        sessionID = sessionIn;
        startTime = startIn;
        deliveredTime = deliveredIn;
        duration = durationIn;
        address = addressIn;
        distance = distanceIn;
    }
}
