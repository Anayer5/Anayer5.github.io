package com.snhu.eventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Read/write access to the {@code events} table. Provides the full CRUD set the
 * dashboard needs: <b>C</b>reate, <b>R</b>ead (per user), <b>U</b>pdate, and
 * <b>D</b>elete.
 */
public class EventRepository {

    private final DatabaseHelper dbHelper;

    public EventRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Create: inserts a new event owned by {@code userId}; returns its new id. */
    public long insertEvent(long userId, Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DatabaseHelper.TABLE_EVENTS, null, toValues(userId, event));
    }

    /** Read: returns every event belonging to {@code userId}, oldest first. */
    public List<Event> getEventsForUser(long userId) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, null,
                DatabaseHelper.COL_EVENT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, DatabaseHelper.COL_EVENT_ID + " ASC")) {
            while (cursor.moveToNext()) {
                events.add(fromCursor(cursor));
            }
        }
        return events;
    }

    /** Update: overwrites an existing event's fields; returns rows affected. */
    public int updateEvent(long userId, Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DatabaseHelper.TABLE_EVENTS, toValues(userId, event),
                DatabaseHelper.COL_EVENT_ID + " = ?",
                new String[]{String.valueOf(event.getId())});
    }

    /** Delete: removes a single event by id; returns rows affected. */
    public int deleteEvent(long eventId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_EVENTS,
                DatabaseHelper.COL_EVENT_ID + " = ?",
                new String[]{String.valueOf(eventId)});
    }

    // --- helpers ---

    /** Maps an Event onto the column/value pairs used for insert and update. */
    private ContentValues toValues(long userId, Event event) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EVENT_USER_ID, userId);
        values.put(DatabaseHelper.COL_EVENT_TITLE, event.getTitle());
        values.put(DatabaseHelper.COL_EVENT_DATE, event.getDateText());
        values.put(DatabaseHelper.COL_EVENT_TIME, event.getTimeText());
        values.put(DatabaseHelper.COL_EVENT_NOTES, event.getNotes());
        values.put(DatabaseHelper.COL_EVENT_SMS, event.isSmsReminder() ? 1 : 0);
        return values;
    }

    /** Reads the current cursor row into an Event object. */
    private Event fromCursor(Cursor cursor) {
        return new Event(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_NOTES)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_SMS)) == 1);
    }
}
