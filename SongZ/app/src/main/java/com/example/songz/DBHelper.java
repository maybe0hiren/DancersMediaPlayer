package com.example.songz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SongzDB";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Songs table
        db.execSQL("CREATE TABLE Songs (id INTEGER PRIMARY KEY AUTOINCREMENT, songName TEXT, songPath TEXT)");
        // Markers table
        db.execSQL("CREATE TABLE Markers (id INTEGER PRIMARY KEY AUTOINCREMENT, songId INTEGER, markerName TEXT, timestamp INTEGER, FOREIGN KEY(songId) REFERENCES Songs(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Markers");
        db.execSQL("DROP TABLE IF EXISTS Songs");
        onCreate(db);
    }

    // Add a song
    public long addSong(String name, String path){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("songName", name);
        values.put("songPath", path);
        return db.insert("Songs", null, values);
    }

    // Add a marker
    public long addMarker(long songId, String markerName, long timestamp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("songId", songId);
        values.put("markerName", markerName);
        values.put("timestamp", timestamp);
        return db.insert("Markers", null, values);
    }

    // Get all songs
    public ArrayList<Song> getAllSongs(){
        ArrayList<Song> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Songs", null);
        if(cursor.moveToFirst()){
            do{
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("songName"));
                String path = cursor.getString(cursor.getColumnIndexOrThrow("songPath"));
                Uri uri = Uri.parse(path);
                Song song = new Song(name, uri); // you'll modify constructor to accept id
                song.setId(id);
                songs.add(song);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    // Get markers for a song
    public ArrayList<Marker> getMarkersForSong(long songId){
        ArrayList<Marker> markers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Markers WHERE songId=?", new String[]{String.valueOf(songId)});
        if(cursor.moveToFirst()){
            do{
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id")); // <-- get DB ID
                String name = cursor.getString(cursor.getColumnIndexOrThrow("markerName"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                Marker marker = new Marker(timestamp, name);
                marker.setId(id);  // <-- set the DB ID here
                markers.add(marker);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return markers;
    }
    public ArrayList<Marker> getMarkersForSongWithIds(long songId){
        ArrayList<Marker> markers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Markers WHERE songId=?", new String[]{String.valueOf(songId)});
        if(cursor.moveToFirst()){
            do{
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("markerName"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                Marker marker = new Marker(timestamp, name);
                marker.setId(id);  // <-- Set the DB ID here
                markers.add(marker);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return markers;
    }
    public long getSongIdByPath(String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM Songs WHERE songPath=?", new String[]{path});
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        }
        cursor.close();
        return id;
    }

    public void deleteSong(long songId){
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete markers
        db.delete("Markers", "songId=?", new String[]{String.valueOf(songId)});
        // Then delete the song
        db.delete("Songs", "id=?", new String[]{String.valueOf(songId)});
    }
    public void deleteMarker(long markerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Markers", "id=?", new String[]{String.valueOf(markerId)});
    }
    public void deleteEverything(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Markers", null, null); // delete all markers
        db.delete("Songs", null, null);   // optional: delete all songs
    }
}
