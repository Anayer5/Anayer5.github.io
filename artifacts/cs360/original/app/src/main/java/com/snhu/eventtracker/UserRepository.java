package com.snhu.eventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Read/write access to the {@code users} table. Keeping all account-related SQL
 * here lets the activities stay focused on the UI.
 *
 * <p>Note: passwords are stored as plain text because this course project is
 * focused on SQLite CRUD. A production app would hash and salt them.</p>
 */
public class UserRepository {

    private final DatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Returns true if an account with this username already exists. */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_ID},
                DatabaseHelper.COL_USERNAME + " = ?",
                new String[]{username}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /**
     * Creates a new account.
     *
     * @return the new user's id, or -1 if the username is already taken.
     */
    public long createUser(String username, String password) {
        if (usernameExists(username)) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USERNAME, username);
        values.put(DatabaseHelper.COL_PASSWORD, password);
        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    /**
     * Checks a login against the stored accounts.
     *
     * @return the matching user's id, or -1 if the credentials are wrong.
     */
    public long authenticate(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_ID},
                DatabaseHelper.COL_USERNAME + " = ? AND " + DatabaseHelper.COL_PASSWORD + " = ?",
                new String[]{username, password}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }
}
