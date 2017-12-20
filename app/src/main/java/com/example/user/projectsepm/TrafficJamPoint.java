package com.example.user.projectsepm;

import com.google.gson.annotations.SerializedName;

/**
 * Created by user on 19/12/2017.
 */

public class TrafficJamPoint {
    @SerializedName("speed")
    private int speed;

    @SerializedName("datetime")
    private String datetime;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    public TrafficJamPoint(int speed,String datetime, double lantitude, double longitude) {
        this.speed = speed;
        this.datetime = datetime;
        this.latitude = lantitude;
        this.longitude = longitude;
    }

//    public String getCurrentDatetime() {
//        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
//        String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());
//        return currentDateTime;
//    }


    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "TrafficJamPoint{" +
                "speed=" + speed +
                ", datetime='" + datetime + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
