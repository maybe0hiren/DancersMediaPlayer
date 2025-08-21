package com.example.songz;

import com.google.android.material.shape.MarkerEdgeTreatment;

import java.util.LinkedList;
import android.net.Uri;
import java.io.File;


public class Song {
    private long id; // DB id
    private String songPath;
    private LinkedList<Marker> markers;
    private String songName;
    private Uri uri;

    public void setId(long id){ this.id = id; }
    public long getId(){ return id; }
    public Song(String songPath){
        this.songPath = songPath;
        this.markers = new LinkedList<>();
        this.songName = new File(songPath).getName();
        this.uri = null;
    }
    public Song(String songName, Uri uri){
        this.songPath = uri.toString();
        this.songName = songName;
        this.uri = uri;
        this.markers = new LinkedList<>();
    }
    public String getSongPath(){
        return songPath;
    }
    public  LinkedList<Marker> getMarkers(){
        return markers;
    }
    public Uri getUri(){
        return uri;
    }
    public String getSongName(){
        return songName;
    }
    public void addMarker(long timestamp, String markerName){
        markers.add(new Marker(timestamp, markerName));
    }
    public void deleteMarker(Marker marker){
        markers.remove(marker);
    }
}
