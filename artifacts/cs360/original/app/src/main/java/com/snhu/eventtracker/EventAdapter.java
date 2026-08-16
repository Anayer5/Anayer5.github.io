package com.snhu.eventtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /** Implemented by DashboardActivity to react to row interactions. */
    public interface OnEventActionListener {
        void onEventClicked(Event event);
        void onDeleteClicked(Event event, int position);
    }

    private final List<Event> events;
    private final OnEventActionListener listener;

    public EventAdapter(List<Event> events, OnEventActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                                 .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.dateTime.setText(event.getDateText() + "  •  " + event.getTimeText());

        if (event.getNotes() == null || event.getNotes().isEmpty()) {
            holder.notes.setVisibility(View.GONE);
        } else {
            holder.notes.setVisibility(View.VISIBLE);
            holder.notes.setText(event.getNotes());
        }

        holder.itemView.setOnClickListener(v -> listener.onEventClicked(event));
        holder.delete.setOnClickListener(v -> listener.onDeleteClicked(event, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView dateTime;
        final TextView notes;
        final ImageButton delete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            dateTime = itemView.findViewById(R.id.event_datetime);
            notes = itemView.findViewById(R.id.event_notes);
            delete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
