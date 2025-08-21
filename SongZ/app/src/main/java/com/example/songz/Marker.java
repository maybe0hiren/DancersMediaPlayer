package com.example.songz;

public class Marker{
    private long id;
    private long timestamp;
    private String markerName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Marker(long timestamp, String markerName){
        this.timestamp = timestamp;
        this.markerName = markerName;
    }

    public long getTimestamp(){
        return timestamp;
    }
    public String getMarkerName(){
        return markerName;
    }
}