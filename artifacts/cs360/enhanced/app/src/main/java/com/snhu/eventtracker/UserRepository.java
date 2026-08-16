package com.snhu.eventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Read/write access to the {@code users} table. Keeping all account-related SQL
 * here lets the activities stay focused on the UI.
 *
 * <p>Enhanced for CS 499: passwords are salted and hashed before storage so
 * the SQLite database never contains a plain-text password.</p>
 */
public class UserRepository {

    private final DatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Package-visible constructor used by instrumentation tests with isolated databases. */
    UserRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** Returns true if an account with this username already exists. */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_ID},
                DatabaseHelper.COL_USERNAME + " = ?",
                new String[]{InputValidator.normalizeUsername(username)}, null, null, null)) {
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
        String normalizedUsername = InputValidator.normalizeUsername(username);
        String salt = PasswordHasher.generateSalt();
        String passwordHash = PasswordHasher.hashPassword(password, salt);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USERNAME, normalizedUsername);
        values.put(DatabaseHelper.COL_PASSWORD_HASH, passwordHash);
        values.put(DatabaseHelper.COL_PASSWORD_SALT, salt);
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
                new String[]{DatabaseHelper.COL_USER_ID, DatabaseHelper.COL_PASSWORD_HASH, DatabaseHelper.COL_PASSWORD_SALT},
                DatabaseHelper.COL_USERNAME + " = ?",
                new String[]{InputValidator.normalizeUsername(username)}, null, null, null)) {
            if (!cursor.moveToFirst()) {
                return -1;
            }
            String storedHash = cursor.getString(1);
            String storedSalt = cursor.getString(2);
            String enteredHash = PasswordHasher.hashPassword(password, storedSalt);
            return PasswordHasher.hashesMatch(storedHash, enteredHash) ? cursor.getLong(0) : -1;
        }
    }
}
