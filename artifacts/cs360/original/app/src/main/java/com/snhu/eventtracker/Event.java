package com.snhu.eventtracker;

/**
 * Plain data holder for a single event row.
 * In Project Three this is loaded from / saved to SQLite via {@link EventRepository}.
 */
public class Event {
    /** Id used for an event that has not been saved to the database yet. */
    public static final long NO_ID = -1;

    private long id;
    private String title;
    private String dateText;     // e.g. "Tue, Jun 18, 2026"
    private String timeText;     // e.g. "2:30 PM"
    private String notes;
    private boolean smsReminder;

    /** Full constructor, used when reading an existing row from the database. */
    public Event(long id, String title, String dateText, String timeText, String notes, boolean smsReminder) {
        this.id = id;
        this.title = title;
        this.dateText = dateText;
        this.timeText = timeText;
        this.notes = notes;
        this.smsReminder = smsReminder;
    }

    /** Convenience constructor for a new event the user just entered (no id yet). */
    public Event(String title, String dateText, String timeText, String notes, boolean smsReminder) {
        this(NO_ID, title, dateText, timeText, notes, smsReminder);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDateText() { return dateText; }
    public String getTimeText() { return timeText; }
    public String getNotes() { return notes; }
    public boolean isSmsReminder() { return smsReminder; }
}
