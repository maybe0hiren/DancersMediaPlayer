package com.example.songz;

import androidx.recyclerview.widget.RecyclerView;
import com.example.songz.R;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Button;
import android.widget.TextView;


public class SongActivity extends AppCompatActivity{
    private DBHelper dbHelper;
    private TextView textPlayingSong;
    private TextView textTimestamp;
    private Song currentSong;
    private RecyclerView recyclerViewMarkers;
    private MarkerAdapter markerAdapter;
    private SeekBar scrollTimeline;
    private Button buttonPlayPause, buttonAddMarker, buttonLoop, buttonBack;
    private boolean loopActive = false;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        String songName = getIntent().getStringExtra("songName");
        String songPath = getIntent().getStringExtra("songPath");
        if (songPath == null) {
            Toast.makeText(this, "Song URI missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentSong = new Song(songPath);
        scrollTimeline = findViewById(R.id.scrollTimeline);
        textPlayingSong = findViewById(R.id.textPlayingSong);
        textTimestamp = findViewById(R.id.textTimestamp);
        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        buttonLoop = findViewById(R.id.buttonLoop);
        buttonAddMarker = findViewById(R.id.buttonAddMarker);
        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewMarkers = findViewById(R.id.recyclerViewMarkers);
        dbHelper = new DBHelper(this);

        Uri uri = Uri.parse(songPath);
        long songId = dbHelper.getSongIdByPath(songPath);
        if (songId == -1) {
            songId = dbHelper.addSong(songName, songPath);
        }
        currentSong = new Song(songName, Uri.parse(songPath));
        currentSong.setId(songId);  // set the DB ID

        currentSong.getMarkers().addAll(dbHelper.getMarkersForSong(songId));
        //currentSong.getMarkers().addAll(dbHelper.getMarkersForSong(currentSong.getId()));
        textPlayingSong.setText(songName);
        try {
            mediaPlayer = MediaPlayer.create(this, uri);
            if (mediaPlayer == null) {
                Toast.makeText(this, "Cannot play song. File missing or inaccessible.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening song.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        textTimestamp.setText("00:00 / " + formatTime(mediaPlayer.getDuration()));
        scrollTimeline.setMax(mediaPlayer.getDuration());
        new Thread(()->{
            while (mediaPlayer != null){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                scrollTimeline.post(() -> {
                    int currentPos = mediaPlayer.getCurrentPosition();
                    textTimestamp.setText(formatTime(currentPos) + "/" + formatTime(mediaPlayer.getDuration()));
                    scrollTimeline.setProgress(mediaPlayer.getCurrentPosition());
                });
            }
        }).start();

        scrollTimeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(fromUser && mediaPlayer != null){
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        markerAdapter = new MarkerAdapter(this, currentSong.getMarkers(), new MarkerAdapter.OnMarkerActionListener(){
           @Override
            public void onJump(Marker marker){
               mediaPlayer.seekTo((int) marker.getTimestamp());
               scrollTimeline.setProgress((int) marker.getTimestamp());
           }
           @Override
            public void onDelete(Marker marker, int position){
               currentSong.deleteMarker(marker);
               markerAdapter.notifyItemRemoved(position);
               markerAdapter.notifyItemRangeChanged(position, currentSong.getMarkers().size());
           }

        });

        recyclerViewMarkers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMarkers.setAdapter(markerAdapter);

        buttonAddMarker.setOnClickListener(v -> {
            int currentPos = mediaPlayer.getCurrentPosition();
            String markerName = "Marker " + (currentSong.getMarkers().size() + 1);
            long dbId = dbHelper.addMarker(currentSong.getId(), markerName, currentPos); // Add to DB once
            Marker newMarker = new Marker(currentPos, markerName);
            newMarker.setId(dbId);  // Set DB ID
            currentSong.getMarkers().add(newMarker);  // Add to memory
            markerAdapter.notifyItemInserted(currentSong.getMarkers().size() - 1);
            recyclerViewMarkers.scrollToPosition(currentSong.getMarkers().size() - 1);
        });
        buttonPlayPause.setOnClickListener(v -> {
            if(mediaPlayer != null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    buttonPlayPause.setText("Play");
                }
                else{
                    mediaPlayer.start();
                    buttonPlayPause.setText("Pause");
                }
            }
        });
        buttonBack.setOnClickListener(v -> {
            mediaPlayer.pause();
            finish();
        });
        buttonLoop.setOnClickListener(v -> {
            loopActive = !loopActive; // toggle the boolean

            if(loopActive){
                buttonLoop.setText("Loop: ON");
            } else {
                buttonLoop.setText("Loop: OFF");
            }
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            if(loopActive){
                mediaPlayer.seekTo(0);  // go back to start
                mediaPlayer.start();    // play again
            } else {
                buttonPlayPause.setText("Play"); // update UI
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.pause();
    }
}
