package com.snhu.eventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Central SQLite helper for Event Tracker.
 *
 * <p>This class owns the database schema and hands a shared connection to the
 * repository classes. Two tables are defined:</p>
 *
 * <ul>
 *   <li>{@code users}  &ndash; one row per account, storing salted password hashes</li>
 *   <li>{@code events} &ndash; one row per saved event, linked to its owner via {@code user_id}</li>
 * </ul>
 *
 * <p>CS 499 final polish: the version 1 to version 2 upgrade now preserves
 * existing users and events instead of dropping tables. Version 1 plain-text
 * passwords are converted into salted PBKDF2 hashes during migration, and the
 * legacy password column is removed by rebuilding the users table.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eventtracker.db";
    private static final int DATABASE_VERSION = 2;

    // --- users table ---
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_PASSWORD_SALT = "password_salt";

    // Version 1 column retained only so migration code can read old databases.
    static final String COL_LEGACY_PASSWORD = "password";

    // --- events table ---
    public static final String TABLE_EVENTS = "events";
    public static final String COL_EVENT_ID = "_id";
    public static final String COL_EVENT_USER_ID = "user_id";
    public static final String COL_EVENT_TITLE = "title";
    public static final String COL_EVENT_DATE = "event_date";
    public static final String COL_EVENT_TIME = "event_time";
    public static final String COL_EVENT_NOTES = "notes";
    public static final String COL_EVENT_SMS = "sms_reminder";
    public static final String COL_EVENT_TIMESTAMP = "event_timestamp";

    private static final String USERS_V1_BACKUP = "users_v1_backup";
    private static final String EVENTS_V1_BACKUP = "events_v1_backup";

    // Single shared instance so every repository reuses the same connection.
    private static DatabaseHelper instance;

    /** Returns the shared helper, creating it against the application context on first use. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /** Resets the singleton only for instrumented tests that need isolated databases. */
    static synchronized void resetInstanceForTests() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    private DatabaseHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    /** Package-visible constructor lets instrumentation tests create isolated database files. */
    DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createEventsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            if (oldVersion < 2) {
                migrateVersion1ToVersion2(db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COL_PASSWORD_HASH + " TEXT NOT NULL, " +
                COL_PASSWORD_SALT + " TEXT NOT NULL)");
    }

    private void createEventsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_USER_ID + " INTEGER NOT NULL, " +
                COL_EVENT_TITLE + " TEXT NOT NULL, " +
                COL_EVENT_DATE + " TEXT, " +
                COL_EVENT_TIME + " TEXT, " +
                COL_EVENT_NOTES + " TEXT, " +
                COL_EVENT_SMS + " INTEGER NOT NULL DEFAULT 0, " +
                COL_EVENT_TIMESTAMP + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(" + COL_EVENT_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + "))");
    }

    /**
     * Migrates the original course-project schema to the enhanced CS 499 schema.
     * Existing user ids are preserved so saved events remain linked to the same owners.
     */
    private void migrateVersion1ToVersion2(SQLiteDatabase db) {
        if (tableExists(db, TABLE_USERS) && columnExists(db, TABLE_USERS, COL_LEGACY_PASSWORD)) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " RENAME TO " + USERS_V1_BACKUP);
            createUsersTable(db);
            copyVersion1UsersIntoVersion2(db);
            db.execSQL("DROP TABLE IF EXISTS " + USERS_V1_BACKUP);
        } else if (!tableExists(db, TABLE_USERS)) {
            createUsersTable(db);
        }

        if (tableExists(db, TABLE_EVENTS) && !columnExists(db, TABLE_EVENTS, COL_EVENT_TIMESTAMP)) {
            db.execSQL("ALTER TABLE " + TABLE_EVENTS + " RENAME TO " + EVENTS_V1_BACKUP);
            createEventsTable(db);
            copyVersion1EventsIntoVersion2(db);
            db.execSQL("DROP TABLE IF EXISTS " + EVENTS_V1_BACKUP);
        } else if (!tableExists(db, TABLE_EVENTS)) {
            createEventsTable(db);
        }
    }

    private void copyVersion1UsersIntoVersion2(SQLiteDatabase db) {
        try (Cursor cursor = db.query(USERS_V1_BACKUP,
                new String[]{COL_USER_ID, COL_USERNAME, COL_LEGACY_PASSWORD},
                null, null, null, null, COL_USER_ID + " ASC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
                String oldPassword = cursor.getString(cursor.getColumnIndexOrThrow(COL_LEGACY_PASSWORD));
                String salt = PasswordHasher.generateSalt();
                String passwordHash = PasswordHasher.hashPassword(oldPassword, salt);

                ContentValues values = new ContentValues();
                values.put(COL_USER_ID, id);
                values.put(COL_USERNAME, InputValidator.normalizeUsername(username));
                values.put(COL_PASSWORD_HASH, passwordHash);
                values.put(COL_PASSWORD_SALT, salt);
                db.insertOrThrow(TABLE_USERS, null, values);
            }
        }
    }

    private void copyVersion1EventsIntoVersion2(SQLiteDatabase db) {
        try (Cursor cursor = db.query(EVENTS_V1_BACKUP,
                new String[]{COL_EVENT_ID, COL_EVENT_USER_ID, COL_EVENT_TITLE, COL_EVENT_DATE,
                        COL_EVENT_TIME, COL_EVENT_NOTES, COL_EVENT_SMS},
                null, null, null, null, COL_EVENT_ID + " ASC")) {
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(COL_EVENT_ID, cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_ID)));
                values.put(COL_EVENT_USER_ID, cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_USER_ID)));
                values.put(COL_EVENT_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TITLE)));
                values.put(COL_EVENT_DATE, cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE)));
                values.put(COL_EVENT_TIME, cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TIME)));
                values.put(COL_EVENT_NOTES, cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_NOTES)));
                values.put(COL_EVENT_SMS, cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_SMS)));
                values.put(COL_EVENT_TIMESTAMP, Event.NO_TIMESTAMP);
                db.insertOrThrow(TABLE_EVENTS, null, values);
            }
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName})) {
            return cursor.moveToFirst();
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
            while (cursor.moveToNext()) {
                String currentColumn = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (columnName.equals(currentColumn)) {
                    return true;
                }
            }
            return false;
        }
    }
}
