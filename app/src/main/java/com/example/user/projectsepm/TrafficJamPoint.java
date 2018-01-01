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

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    public TrafficJamPoint(int speed,String datetime, double lantitude, double longitude) {
        this.speed = speed;
        this.datetime = datetime;
        this.lat = lantitude;
        this.lon = longitude;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "TrafficJamPoint{" +
                "speed=" + speed +
                ", datetime='" + datetime + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
