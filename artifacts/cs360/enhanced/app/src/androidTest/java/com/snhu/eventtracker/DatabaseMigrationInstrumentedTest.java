package com.snhu.eventtracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Instrumented test proving version 1 users/events survive the version 2 migration. */
@RunWith(AndroidJUnit4.class)
public class DatabaseMigrationInstrumentedTest {

    private static final String TEST_DB = "eventtracker_migration_test.db";

    private Context context;
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(TEST_DB);
        createVersionOneDatabase();
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        context.deleteDatabase(TEST_DB);
        DatabaseHelper.resetInstanceForTests();
    }

    @Test
    public void migrationFromVersionOne_preservesUsersEventsAndHashesPasswords() {
        dbHelper = new DatabaseHelper(context, TEST_DB);
        SQLiteDatabase migratedDb = dbHelper.getWritableDatabase();

        assertTrue(columnExists(migratedDb, DatabaseHelper.TABLE_USERS, DatabaseHelper.COL_PASSWORD_HASH));
        assertTrue(columnExists(migratedDb, DatabaseHelper.TABLE_USERS, DatabaseHelper.COL_PASSWORD_SALT));
        assertFalse(columnExists(migratedDb, DatabaseHelper.TABLE_USERS, DatabaseHelper.COL_LEGACY_PASSWORD));
        assertTrue(columnExists(migratedDb, DatabaseHelper.TABLE_EVENTS, DatabaseHelper.COL_EVENT_TIMESTAMP));

        assertEquals(1, countRows(migratedDb, DatabaseHelper.TABLE_USERS));
        assertEquals(1, countRows(migratedDb, DatabaseHelper.TABLE_EVENTS));

        try (Cursor cursor = migratedDb.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_ID, DatabaseHelper.COL_USERNAME,
                        DatabaseHelper.COL_PASSWORD_HASH, DatabaseHelper.COL_PASSWORD_SALT},
                null, null, null, null, null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(1L, cursor.getLong(0));
            assertEquals("ashernayer", cursor.getString(1));
            assertNotEquals("OriginalPassword123", cursor.getString(2));
            assertNotEquals("OriginalPassword123", cursor.getString(3));
        }

        UserRepository userRepository = new UserRepository(dbHelper);
        assertEquals(1L, userRepository.authenticate("AsherNayer", "OriginalPassword123"));

        try (Cursor cursor = migratedDb.query(DatabaseHelper.TABLE_EVENTS,
                new String[]{DatabaseHelper.COL_EVENT_ID, DatabaseHelper.COL_EVENT_USER_ID,
                        DatabaseHelper.COL_EVENT_TITLE, DatabaseHelper.COL_EVENT_TIMESTAMP},
                null, null, null, null, null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(10L, cursor.getLong(0));
            assertEquals(1L, cursor.getLong(1));
            assertEquals("Capstone Review", cursor.getString(2));
            assertEquals(Event.NO_TIMESTAMP, cursor.getLong(3));
        }
    }

    private void createVersionOneDatabase() {
        SQLiteDatabase db = context.openOrCreateDatabase(TEST_DB, Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE users (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL)");
        db.execSQL("CREATE TABLE events (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "event_date TEXT, " +
                "event_time TEXT, " +
                "notes TEXT, " +
                "sms_reminder INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("INSERT INTO users (_id, username, password) VALUES " +
                "(1, 'AsherNayer', 'OriginalPassword123')");
        db.execSQL("INSERT INTO events (_id, user_id, title, event_date, event_time, notes, sms_reminder) " +
                "VALUES (10, 1, 'Capstone Review', '2026-08-10', '10:00 AM', 'Review work', 1)");
        db.setVersion(1);
        db.close();
    }

    private int countRows(SQLiteDatabase db, String tableName) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    return true;
                }
            }
            return false;
        }
    }
}
