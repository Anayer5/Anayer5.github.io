package com.snhu.eventtracker;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Form used to both add a new event and edit an existing one (Create / Update).
 *
 * <p>When launched with {@link #EXTRA_EVENT_ID} the form pre-fills with that
 * event's data and saving performs an update; otherwise saving inserts a new
 * row. When the SMS reminder switch is on and the user has granted permission,
 * an SMS alert is sent on save; if permission is denied the event is still
 * saved and the rest of the app keeps working.</p>
 */
public class AddEventActivity extends AppCompatActivity {

    /** Optional extra: the id of the event being edited (absent when adding). */
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    // Demo recipient for reminder texts. In a real app this would be the user's
    // own number from their profile; for emulator testing any number works.
    private static final String REMINDER_PHONE_NUMBER = "15555215554";

    private TextInputLayout titleLayout;
    private TextInputEditText titleInput;
    private TextInputEditText notesInput;
    private MaterialButton btnPickDate;
    private MaterialButton btnPickTime;
    private MaterialSwitch smsSwitch;
    private View rootView;

    private EventRepository eventRepository;
    private long userId;
    private long editingEventId = Event.NO_ID;   // NO_ID = adding a new event

    // Display text for the chosen date/time; empty until the user picks one.
    private String dateText = "";
    private String timeText = "";

    // Selected date/time held in member fields so re-opening a picker keeps state.
    private Integer year, month, day;     // null until the user picks a date
    private Integer hour, minute;         // null until the user picks a time

    // Permission result handler for SEND_SMS.
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    Snackbar.make(rootView, R.string.msg_sms_enabled, Snackbar.LENGTH_SHORT).show();
                } else {
                    smsSwitch.setChecked(false);
                    Snackbar.make(rootView, R.string.sms_permission_denied, Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventRepository = new EventRepository(this);
        userId = getIntent().getLongExtra(DashboardActivity.EXTRA_USER_ID, -1);
        editingEventId = getIntent().getLongExtra(EXTRA_EVENT_ID, Event.NO_ID);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rootView = findViewById(R.id.add_event_root);
        titleLayout = findViewById(R.id.title_layout);
        titleInput = findViewById(R.id.title_input);
        notesInput = findViewById(R.id.notes_input);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickTime = findViewById(R.id.btn_pick_time);
        smsSwitch = findViewById(R.id.sms_switch);
        MaterialButton btnSave = findViewById(R.id.btn_save);
        MaterialButton btnCancel = findViewById(R.id.btn_cancel);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> onSaveClicked());
        btnCancel.setOnClickListener(v -> finish());

        smsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onSmsSwitchEnabled();
            }
            // Turning the switch OFF needs no action.
        });

        if (editingEventId != Event.NO_ID) {
            // Edit mode: change the title and pre-fill the form from the database.
            toolbar.setTitle(R.string.edit_event_title);
            prefillFromExistingEvent();
        }
    }

    /** Loads the event being edited and copies its values into the form. */
    private void prefillFromExistingEvent() {
        Event existing = findEventById(editingEventId);
        if (existing == null) {
            return;   // event was deleted elsewhere; fall back to add mode
        }
        titleInput.setText(existing.getTitle());
        notesInput.setText(existing.getNotes());

        dateText = existing.getDateText() == null ? "" : existing.getDateText();
        timeText = existing.getTimeText() == null ? "" : existing.getTimeText();
        if (!dateText.isEmpty()) {
            btnPickDate.setText(dateText);
        }
        if (!timeText.isEmpty()) {
            btnPickTime.setText(timeText);
        }
        smsSwitch.setChecked(existing.isSmsReminder());
    }

    /** Finds a single event for the current user by id (used to pre-fill edit mode). */
    private Event findEventById(long eventId) {
        return eventRepository.getEventForUser(userId, eventId);
    }

    private void onSmsSwitchEnabled() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // Already granted: leave the switch ON, no dialog.
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            // Previously denied once: explain why we need it before asking again.
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.sms_permission_title)
                    .setMessage(R.string.sms_permission_rationale)
                    .setPositiveButton(R.string.btn_grant,
                            (d, w) -> smsPermissionLauncher.launch(Manifest.permission.SEND_SMS))
                    .setNegativeButton(R.string.btn_not_now,
                            (d, w) -> smsSwitch.setChecked(false))
                    .show();
        } else {
            // First request (or "don't ask again"): ask the system directly.
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        int initYear  = year  != null ? year  : c.get(Calendar.YEAR);
        int initMonth = month != null ? month : c.get(Calendar.MONTH);
        int initDay   = day   != null ? day   : c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            year = y;
            month = m;
            day = d;
            Calendar picked = Calendar.getInstance();
            picked.set(y, m, d);
            // e.g. "Tue, Jun 18, 2026"
            dateText = DateFormat.format("EEE, MMM d, yyyy", picked).toString();
            btnPickDate.setText(dateText);
        }, initYear, initMonth, initDay);
        dialog.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        int initHour   = hour   != null ? hour   : c.get(Calendar.HOUR_OF_DAY);
        int initMinute = minute != null ? minute : c.get(Calendar.MINUTE);
        boolean is24h = DateFormat.is24HourFormat(this);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, min) -> {
            hour = h;
            minute = min;
            Calendar picked = Calendar.getInstance();
            picked.set(Calendar.HOUR_OF_DAY, h);
            picked.set(Calendar.MINUTE, min);
            String pattern = is24h ? "HH:mm" : "h:mm a";
            timeText = DateFormat.format(pattern, picked).toString();
            btnPickTime.setText(timeText);
        }, initHour, initMinute, is24h);
        dialog.show();
    }

    private void onSaveClicked() {
        String title = titleInput.getText() == null ? "" : titleInput.getText().toString().trim();
        if (!InputValidator.isEventTitleValid(title)) {
            titleLayout.setError("Enter an event title between 1 and 80 characters.");
            return;
        }
        titleLayout.setError(null);

        String notes = notesInput.getText() == null ? "" : notesInput.getText().toString().trim();
        // Fall back to friendly placeholders so the grid never shows a blank line.
        String displayDate = TextUtils.isEmpty(dateText) ? getString(R.string.no_date_set) : dateText;
        String displayTime = TextUtils.isEmpty(timeText) ? getString(R.string.no_time_set) : timeText;
        boolean smsReminder = smsSwitch.isChecked();
        long eventTimestamp = selectedEventTimestamp();

        Event event = new Event(editingEventId, title, displayDate, displayTime, notes, smsReminder, eventTimestamp);

        if (editingEventId == Event.NO_ID) {
            // Create
            eventRepository.insertEvent(userId, event);
            Toast.makeText(this, R.string.msg_event_saved, Toast.LENGTH_SHORT).show();
        } else {
            // Update
            eventRepository.updateEvent(userId, event);
            Toast.makeText(this, R.string.msg_event_updated, Toast.LENGTH_SHORT).show();
        }

        // Send an SMS alert only when the user asked for it AND granted permission.
        if (smsReminder) {
            sendSmsReminderIfPermitted(event);
        }
        finish();
    }

    /**
     * Builds a machine-readable timestamp when both date and time are selected.
     * The display strings remain user-friendly, while the repository can sort and
     * schedule from a numeric value.
     */
    private long selectedEventTimestamp() {
        if (year == null || month == null || day == null || hour == null || minute == null) {
            return Event.NO_TIMESTAMP;
        }
        Calendar selected = Calendar.getInstance();
        selected.set(Calendar.YEAR, year);
        selected.set(Calendar.MONTH, month);
        selected.set(Calendar.DAY_OF_MONTH, day);
        selected.set(Calendar.HOUR_OF_DAY, hour);
        selected.set(Calendar.MINUTE, minute);
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);
        return selected.getTimeInMillis();
    }

    /**
     * Sends an SMS reminder for the event, but only if the SEND_SMS permission is
     * granted. If it is not, the method simply returns so the rest of the app
     * continues to work without the SMS feature.
     */
    private void sendSmsReminderIfPermitted(Event event) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return;   // permission denied: silently skip the SMS, app keeps working
        }
        try {
            String message = getString(R.string.sms_reminder_body,
                    event.getTitle(), event.getDateText());
            getSmsManager().sendTextMessage(REMINDER_PHONE_NUMBER, null, message, null, null);
            Snackbar.make(rootView, R.string.msg_sms_sent, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Never let a messaging failure crash the app or block saving.
            Snackbar.make(rootView, R.string.msg_sms_failed, Snackbar.LENGTH_LONG).show();
        }
    }

    /** Returns an SmsManager using the API appropriate for the device version. */
    @SuppressWarnings("deprecation")
    private SmsManager getSmsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return getSystemService(SmsManager.class);
        }
        return SmsManager.getDefault();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
