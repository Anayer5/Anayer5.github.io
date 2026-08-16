package com.snhu.eventtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Central SQLite helper for Event Tracker.
 *
 * <p>This class owns the database schema (the "shell") and hands a single shared
 * connection to the repository classes. Two tables are defined:</p>
 *
 * <ul>
 *   <li>{@code users}  &ndash; one row per account (login credentials)</li>
 *   <li>{@code events} &ndash; one row per saved event, linked to its owner via {@code user_id}</li>
 * </ul>
 *
 * <p>The database file is persistent, so accounts and events survive app restarts.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eventtracker.db";
    private static final int DATABASE_VERSION = 1;

    // --- users table ---
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // --- events table ---
    public static final String TABLE_EVENTS = "events";
    public static final String COL_EVENT_ID = "_id";
    public static final String COL_EVENT_USER_ID = "user_id";
    public static final String COL_EVENT_TITLE = "title";
    public static final String COL_EVENT_DATE = "event_date";
    public static final String COL_EVENT_TIME = "event_time";
    public static final String COL_EVENT_NOTES = "notes";
    public static final String COL_EVENT_SMS = "sms_reminder";

    // Single shared instance so every repository reuses the same connection.
    private static DatabaseHelper instance;

    /** Returns the shared helper, creating it (against the application context) on first use. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Account table: username is unique so two people can't share a login.
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COL_PASSWORD + " TEXT NOT NULL)");

        // Event table: each event belongs to one user via user_id.
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_USER_ID + " INTEGER NOT NULL, " +
                COL_EVENT_TITLE + " TEXT NOT NULL, " +
                COL_EVENT_DATE + " TEXT, " +
                COL_EVENT_TIME + " TEXT, " +
                COL_EVENT_NOTES + " TEXT, " +
                COL_EVENT_SMS + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(" + COL_EVENT_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Project Three ships schema v1 only. A production app would migrate data
        // here; for the shell we simply drop and recreate the tables.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
