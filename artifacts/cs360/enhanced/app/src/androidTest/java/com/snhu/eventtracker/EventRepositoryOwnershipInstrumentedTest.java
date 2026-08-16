package com.snhu.eventtracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Instrumented tests for user-scoped event update and delete behavior. */
@RunWith(AndroidJUnit4.class)
public class EventRepositoryOwnershipInstrumentedTest {

    private static final String TEST_DB = "eventtracker_ownership_test.db";

    private Context context;
    private DatabaseHelper dbHelper;
    private EventRepository eventRepository;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(TEST_DB);
        dbHelper = new DatabaseHelper(context, TEST_DB);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO users (_id, username, password_hash, password_salt) " +
                "VALUES (1, 'userone', 'hash1', 'salt1')");
        db.execSQL("INSERT INTO users (_id, username, password_hash, password_salt) " +
                "VALUES (2, 'usertwo', 'hash2', 'salt2')");
        eventRepository = new EventRepository(dbHelper);
    }

    @After
    public void tearDown() {
        dbHelper.close();
        context.deleteDatabase(TEST_DB);
        DatabaseHelper.resetInstanceForTests();
    }

    @Test
    public void updateEvent_doesNotModifyEventOwnedByAnotherUser() {
        long eventId = eventRepository.insertEvent(2, new Event(
                "Owner Two Event", "2026-08-10", "10:00 AM", "private", false, 1000L));

        int rowsUpdated = eventRepository.updateEvent(1, new Event(
                eventId, "Tampered Event", "2026-08-10", "11:00 AM", "wrong user", false, 2000L));

        assertEquals(0, rowsUpdated);
        assertNull(eventRepository.getEventForUser(1, eventId));
        assertEquals("Owner Two Event", eventRepository.getEventForUser(2, eventId).getTitle());
    }

    @Test
    public void deleteEvent_doesNotDeleteEventOwnedByAnotherUser() {
        long eventId = eventRepository.insertEvent(2, new Event(
                "Owner Two Event", "2026-08-10", "10:00 AM", "private", false, 1000L));

        int rowsDeleted = eventRepository.deleteEvent(1, eventId);

        assertEquals(0, rowsDeleted);
        assertNotNull(eventRepository.getEventForUser(2, eventId));
    }

    @Test
    public void updateAndDeleteEvent_workForOwningUser() {
        long eventId = eventRepository.insertEvent(1, new Event(
                "Original Event", "2026-08-10", "10:00 AM", "notes", false, 1000L));

        int rowsUpdated = eventRepository.updateEvent(1, new Event(
                eventId, "Updated Event", "2026-08-10", "11:00 AM", "updated", true, 2000L));
        int rowsDeleted = eventRepository.deleteEvent(1, eventId);

        assertEquals(1, rowsUpdated);
        assertEquals(1, rowsDeleted);
        assertNull(eventRepository.getEventForUser(1, eventId));
    }
}
