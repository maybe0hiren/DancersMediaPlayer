package com.example.songz;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import android.net.Uri;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> songQueue = new ArrayList<>();
    private SongAdapter songAdapter;
    private Button buttonAddSong;
    private ActivityResultLauncher<String> pickAudioLauncher;
    private RecyclerView recyclerView;
    private DBHelper dbHelper;

    protected void onDestroy() {
        super.onDestroy();
        // Clear all markers
        if(dbHelper != null) {
            dbHelper.deleteEverything();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        buttonAddSong = findViewById(R.id.buttonAddSong);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
        recyclerView.setAdapter(songAdapter);
        dbHelper = new DBHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(this, songQueue);
        recyclerView.setAdapter(songAdapter);

//        songQueue.addAll(dbHelper.getAllSongs());
//        for(Song s : songQueue){
//            s.getMarkers().addAll(dbHelper.getMarkersForSong(s.getId()));
//        }
        songAdapter.notifyDataSetChanged();

        pickAudioLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null){
                        handlePickedSong(uri);
                    }
                    else{
                        Toast.makeText(this, "No song selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        buttonAddSong.setOnClickListener(v->{
            pickAudioLauncher.launch("audio/*");
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private String getFileNameFromUri(Uri uri) {
        String result = "Unknown Song";
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            result = new java.io.File(uri.getPath()).getName();
        }
        return result;
    }

    private void handlePickedSong(Uri uri){
        String songName = getFileNameFromUri(uri);
        long songId = dbHelper.addSong(songName, uri.toString());
        Song newSong = new Song(songName, uri);
        newSong.setId(songId);
        songQueue.add(newSong);
        songAdapter.notifyItemInserted(songQueue.size() - 1);
        recyclerView.scrollToPosition(songQueue.size() - 1);
//        Toast.makeText(this, "Song added! Total: " + songQueue.size(), Toast.LENGTH_SHORT).show();
    }
}