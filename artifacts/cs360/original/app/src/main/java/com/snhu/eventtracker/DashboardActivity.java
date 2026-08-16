package com.snhu.eventtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen after login. Reads the signed-in user's events from SQLite and
 * displays them as a two-column grid. Supports delete (here) and add/edit
 * (via {@link AddEventActivity}); together with the repository this provides
 * the full CRUD experience.
 */
public class DashboardActivity extends AppCompatActivity
        implements EventAdapter.OnEventActionListener {

    /** Intent extra carrying the signed-in user's id between screens. */
    public static final String EXTRA_USER_ID = "extra_user_id";

    /** Number of columns in the event grid. */
    private static final int GRID_COLUMNS = 2;

    private List<Event> events = new ArrayList<>();
    private EventAdapter adapter;
    private RecyclerView eventList;
    private TextView emptyState;

    private EventRepository eventRepository;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        eventRepository = new EventRepository(this);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);

        eventList = findViewById(R.id.event_list);
        emptyState = findViewById(R.id.empty_state);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        adapter = new EventAdapter(events, this);
        // Display the database contents as a grid (Read).
        eventList.setLayoutManager(new GridLayoutManager(this, GRID_COLUMNS));
        eventList.setAdapter(adapter);

        fab.setOnClickListener(v -> onAddClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload from the database every time we return so newly added or edited
        // events appear immediately.
        loadEvents();
    }

    /** Read: pull this user's events from SQLite and refresh the grid. */
    private void loadEvents() {
        events.clear();
        events.addAll(eventRepository.getEventsForUser(userId));
        adapter.notifyDataSetChanged();
        refreshEmptyState();
    }

    private void onAddClicked() {
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onEventClicked(Event event) {
        // Update: open the form pre-filled with this event so the user can edit it.
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(AddEventActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onDeleteClicked(Event event, int position) {
        // Delete: confirm, then remove the row from the database and the grid.
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, event.getTitle()))
            .setNegativeButton(R.string.btn_cancel, null)
            .setPositiveButton(R.string.btn_delete, (d, w) -> {
                eventRepository.deleteEvent(event.getId());
                events.remove(position);
                adapter.notifyItemRemoved(position);
                refreshEmptyState();
            })
            .show();
    }

    private void refreshEmptyState() {
        boolean empty = events.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        eventList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
