package com.example.songz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder>{
    private Context context;
    private ArrayList<Song> songQueue;

    public SongAdapter(Context context, ArrayList<Song> songQueue){
        this.context = context;
        this.songQueue = songQueue;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position){
        Song song = songQueue.get(position);
        holder.songTitle.setText(song.getSongName());
        holder.buttonDelete.setOnClickListener(v->{
            Song songToDelete = songQueue.get(position);
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.deleteSong(songToDelete.getId());
            songQueue.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, songQueue.size());
            Toast.makeText(context, "Song Deleted", Toast.LENGTH_SHORT).show();
        });
        holder.buttonJump.setOnClickListener(v->{
//            Toast.makeText(context, "Jumping to: " + song.getSongPath(), Toast.LENGTH_SHORT).show();
            Context context = v.getContext();
            Intent intent = new Intent(context, SongActivity.class);
            intent.putExtra("songName", song.getSongName());
            intent.putExtra("songPath", song.getSongPath());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songQueue.size();
    }
    public static class SongViewHolder extends RecyclerView.ViewHolder{
        TextView songTitle;
        Button buttonDelete, buttonJump;
        public SongViewHolder(@NonNull View itemView){
            super(itemView);
            songTitle = itemView.findViewById(R.id.songTitle);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonJump = itemView.findViewById(R.id.buttonJump);

        }
    }
}
