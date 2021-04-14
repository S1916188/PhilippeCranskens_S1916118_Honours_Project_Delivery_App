package com.example.pizzadeliveryapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class Session implements Serializable
{
    //Attributes
    private int sessionID;
    private int active;
    private String date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int deliveries;
    private int userId;

    public int getSessionID() {return sessionID;}
    public void setSessionID(int sessionID) {this.sessionID = sessionID;}

    public int getActive() {return active;}
    public void setActive(int active) {this.active = active;}

    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}

    public LocalTime getStartTime() {return startTime;}
    public void setStartTime(LocalTime startTime) {this.startTime = startTime;}

    public LocalTime getEndTime() {return endTime;}
    public void setEndTime(LocalTime endTime) {this.endTime = endTime;}

    public int getDeliveries() {return deliveries;}
    public void setDeliveries(int deliveries) {this.deliveries = deliveries;}

    public int getUserId() {return userId;}
    public void setUserId(int userId) {this.userId = userId;}

    @RequiresApi(api = Build.VERSION_CODES.O)
    // Constructors
    public Session()
    {
        sessionID = 0;
        active = 1;
        date = Calendar.getInstance().toString();
        startTime = LocalTime.now();
        endTime = LocalTime.now();
        deliveries = 0;
        userId = 0;
    }

    public Session(int sessionIn, int activeIn, String dateIn, LocalTime startIn, LocalTime endIn, int deliveriesIn, int userIn)
    {
        sessionID = sessionIn;
        active = activeIn;
        date = dateIn;
        startTime = startIn;
        endTime = endIn;
        deliveries = deliveriesIn;
        userId = userIn;
    }

}
