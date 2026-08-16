package com.snhu.eventtracker;

/**
 * Plain data holder for a single event row.
 * In Project Three this is loaded from / saved to SQLite via {@link EventRepository}.
 */
public class Event {
    /** Id used for an event that has not been saved to the database yet. */
    public static final long NO_ID = -1;

    /** Timestamp value used when the user has not selected a computable date/time. */
    public static final long NO_TIMESTAMP = 0;

    private long id;
    private String title;
    private String dateText;     // e.g. "Tue, Jun 18, 2026"
    private String timeText;     // e.g. "2:30 PM"
    private String notes;
    private boolean smsReminder;
    private long eventTimestamp; // machine-readable date/time used for sorting and future reminders

    /** Full constructor, used when reading an existing row from the database. */
    public Event(long id, String title, String dateText, String timeText, String notes, boolean smsReminder) {
        this(id, title, dateText, timeText, notes, smsReminder, NO_TIMESTAMP);
    }

    /** Full enhanced constructor, including the computable event timestamp. */
    public Event(long id, String title, String dateText, String timeText, String notes,
                 boolean smsReminder, long eventTimestamp) {
        this.id = id;
        this.title = title;
        this.dateText = dateText;
        this.timeText = timeText;
        this.notes = notes;
        this.smsReminder = smsReminder;
        this.eventTimestamp = eventTimestamp;
    }

    /** Convenience constructor for a new event the user just entered (no id yet). */
    public Event(String title, String dateText, String timeText, String notes, boolean smsReminder) {
        this(NO_ID, title, dateText, timeText, notes, smsReminder, NO_TIMESTAMP);
    }

    /** Convenience constructor for a new enhanced event with a timestamp. */
    public Event(String title, String dateText, String timeText, String notes,
                 boolean smsReminder, long eventTimestamp) {
        this(NO_ID, title, dateText, timeText, notes, smsReminder, eventTimestamp);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDateText() { return dateText; }
    public String getTimeText() { return timeText; }
    public String getNotes() { return notes; }
    public boolean isSmsReminder() { return smsReminder; }
    public long getEventTimestamp() { return eventTimestamp; }
}
