package com.example.songz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MarkerAdapter extends RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder> {

    private Context context;
    private List<Marker> markers; // can be a LinkedList or ArrayList
    private OnMarkerActionListener listener;

    public interface OnMarkerActionListener {
        void onJump(Marker marker);
        void onDelete(Marker marker, int position);
    }

    public MarkerAdapter(Context context, List<Marker> markers, OnMarkerActionListener listener) {
        this.context = context;
        this.markers = markers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MarkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_marker, parent, false);
        return new MarkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarkerViewHolder holder, int position) {
        Marker marker = markers.get(position);
        holder.markerText.setText(marker.getMarkerName() + " (" + formatTimestamp(marker.getTimestamp()) + ")");

        holder.buttonJumpMarker.setOnClickListener(v -> listener.onJump(marker));

        holder.buttonDeleteMarker.setOnClickListener(v -> {
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.deleteMarker(marker.getId());
            listener.onDelete(marker, position);
        });
    }

    @Override
    public int getItemCount() {
        return markers.size();
    }

    public static class MarkerViewHolder extends RecyclerView.ViewHolder {
        TextView markerText;
        Button buttonJumpMarker, buttonDeleteMarker;

        public MarkerViewHolder(@NonNull View itemView) {
            super(itemView);
            markerText = itemView.findViewById(R.id.markerText);
            buttonJumpMarker = itemView.findViewById(R.id.buttonJumpMarker);
            buttonDeleteMarker = itemView.findViewById(R.id.buttonDeleteMarker);
        }
    }

    private String formatTimestamp(long ms) {
        int totalSec = (int) (ms / 1000);
        int minutes = totalSec / 60;
        int seconds = totalSec % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
