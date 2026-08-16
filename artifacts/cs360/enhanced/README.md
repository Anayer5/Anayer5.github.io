# Event Tracker

A simple, privacy-focused Android app for keeping track of personal events. Users
create an account, log in, and manage a list of events (title, date, time, and
notes) stored locally in SQLite. Events are shown as a grid, and — if the user
grants permission — the app sends an SMS reminder for events that have a reminder
turned on.

**Course:** CS-360 Mobile Architecture and Programming
**Package:** `com.snhu.eventtracker` · **minSdk:** 26 (Android 8.0) · **targetSdk:** 36

---

## App design and development reflection

### Requirements, goals, and user needs

Event Tracker was built to give a user a fast, private way to record and review
the events that matter to them without needing a cloud account or sign-up server.
The core requirements were a working login backed by a database, persistent
storage with full create/read/update/delete (CRUD) for events, a grid display of
the stored data, and opt-in SMS reminders that respect the user's permission
choice. The app addresses the everyday need to remember appointments, deadlines,
and important dates — and the equally important need to keep that personal
information on the device rather than in someone else's cloud.

### Screens, features, and a user-centered UI

Three screens carry the experience: a **Login** screen (log in or create an
account), a **Dashboard** that lists every event as a two-column grid with a
floating "+" button, and an **Add/Edit Event** form (one screen reused for both
creating and editing). The designs keep the user in mind by reducing friction at
every step: the login screen lets brand-new users create an account inline rather
than hitting a dead end; the dashboard surfaces all events at a glance with an
empty-state hint when the list is blank; the form uses native date and time
pickers so input is tap-driven and error-proof; and destructive actions (delete)
ask for confirmation. The SMS permission is requested only at the moment the user
turns on a reminder — with a plain-language explanation — instead of demanding it
up front. These choices were successful because they match what users already
expect from Android apps (Material components, familiar pickers, clear feedback
via toasts and snackbars) and because the app keeps working fully even when the
user declines a permission.

### Coding approach, techniques, and strategies

I built the app in layers and separated concerns so each class stays small and
focused. A single `DatabaseHelper` (an `SQLiteOpenHelper` singleton) owns the
schema "shell," and two repository classes — `UserRepository` and
`EventRepository` — hold all the SQL, so the activities only deal with UI. The
`Event` class is a plain data holder, and `EventAdapter` binds events to the
RecyclerView grid. This repository pattern made the code easier to read, test,
and change, and it is a strategy I can reuse on any future project: isolate data
access behind a small API so the rest of the app does not depend on how the data
is stored. I also leaned on industry-standard practices throughout — descriptive
naming, in-line comments explaining intent, consistent style, and graceful
handling of edge cases (denied permissions, empty fields, missing rows).

### Testing for functionality

I tested by compiling the project and building the debug APK after each major
change to catch errors early, and by exercising the app's flows on the Android
Emulator: creating an account, logging in, adding events and seeing them appear
in the grid, editing and deleting them, and — critically — running the SMS
feature down **both** branches (granting permission to confirm a reminder is
sent, and denying it to confirm the rest of the app still works with no SMS and
no error). This testing matters because it verifies the app behaves correctly for
real users, not just that it compiles; it is especially important for the
permission path, where the requirement is that denial must never break the app.
Testing surfaced exactly the kind of issues unit-only checks miss — for example,
making sure the grid refreshes after returning from the add/edit screen and that
a denied permission cleanly resets the reminder switch.

### Where I had to innovate

The biggest challenge came at build and packaging time rather than in the feature
code. Building from the command line failed because the toolchain picked up a JRE
without `jlink`, so I had to identify the correct JDK (Android Studio's bundled
JBR) and direct Gradle to it. I also needed to turn a single high-resolution icon
into a proper Android launcher icon across all screen densities; with no image
editor on hand, I wrote a small Java image-resizing utility (using `javax.imageio`
and `Graphics2D`) to generate the density-specific assets and wired them into an
adaptive icon. Working around tooling gaps with a small purpose-built script,
rather than getting blocked, was the key innovation.

### Component I'm most proud of

The database layer is where I best demonstrated my skills. Designing a clean
SQLite schema for users and events, exposing it through focused repository classes
with the full CRUD set, and connecting it to a live RecyclerView grid that updates
as the user creates, edits, and deletes records tied the whole app together. It is
the foundation everything else — login, the grid, and the reminders — depends on,
and it shows a solid grasp of persistent data, separation of concerns, and
Android's data-binding patterns.

---

## Project structure

| Area | Files |
|---|---|
| Database / data access | `DatabaseHelper.java`, `UserRepository.java`, `EventRepository.java`, `Event.java` |
| Screens | `LoginActivity.java`, `DashboardActivity.java`, `AddEventActivity.java` |
| Grid | `EventAdapter.java`, `res/layout/event_item.xml`, `res/layout/activity_dashboard.xml` |
| Launch plan | `LAUNCH_PLAN.md` |

## Building and running

1. Open the project in Android Studio (it ships the JDK the build needs).
2. Select an emulator or device running **Android 8.0 (API 26)** or newer.
3. Click **Run**, or build a debug APK from the terminal:

   ```bash
   ./gradlew assembleDebug
   ```

   The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Permissions

The app requests only `SEND_SMS`, and only when the user enables a reminder. If
the permission is denied, every other feature continues to work.

---

## CS 499 Software Design and Engineering Enhancement

This enhanced version addresses the planned CS 499 Milestone Two software-design goals. The original app already provided a functional Android event tracker with login, local SQLite persistence, a dashboard, and add/edit/delete workflows. The enhancement makes the design more secure, maintainable, and reviewable by improving the repository layer, authentication logic, input validation, event ownership checks, and event time storage.

### Enhancement summary

- Added `PasswordHasher.java` to replace plain-text password storage with PBKDF2 salted password hashing.
- Added `InputValidator.java` to centralize validation rules for usernames, passwords, and event titles.
- Updated `DatabaseHelper.java` to use schema version 2 with `password_hash`, `password_salt`, and `event_timestamp` columns.
- Updated `UserRepository.java` so account creation stores a salt/hash pair and login verifies the entered password against the stored hash.
- Updated `EventRepository.java` so update and delete operations check both `event_id` and `user_id`, preventing cross-user event modification.
- Added `getEventForUser()` so the edit screen can load only events owned by the authenticated user.
- Updated `Event.java` to include a machine-readable timestamp field while preserving user-friendly date and time display text.
- Updated `AddEventActivity.java` to use centralized event-title validation and compute a timestamp when date and time are selected.
- Updated `DashboardActivity.java` to pass the current user ID into delete operations.
- Added `InputValidatorTest.java` with unit tests for validation rules.

### Security and design rationale

The original artifact was appropriate for a course project, but the most important design weakness was that the user table stored plain-text passwords. The enhanced version stores only a random salt and a derived password hash. This reduces risk if the local SQLite database is inspected. The event repository was also improved so update and delete actions are user-scoped, which is a safer design for multi-user data even in a local app.

### Files most relevant to the enhancement

- `app/src/main/java/com/snhu/eventtracker/PasswordHasher.java`
- `app/src/main/java/com/snhu/eventtracker/InputValidator.java`
- `app/src/main/java/com/snhu/eventtracker/DatabaseHelper.java`
- `app/src/main/java/com/snhu/eventtracker/UserRepository.java`
- `app/src/main/java/com/snhu/eventtracker/EventRepository.java`
- `app/src/main/java/com/snhu/eventtracker/Event.java`
- `app/src/main/java/com/snhu/eventtracker/AddEventActivity.java`
- `app/src/main/java/com/snhu/eventtracker/DashboardActivity.java`
- `app/src/test/java/com/snhu/eventtracker/InputValidatorTest.java`

---

## Final ePortfolio polish after instructor feedback

The final ePortfolio version incorporates the instructor's Milestone Two feedback.
The original Milestone Two enhancement introduced PBKDF2 password hashing,
centralized validation, user-scoped event operations, and machine-readable event
timestamps. The final polish strengthens the evidence for those improvements.

### Final polish changes

- Replaced the destructive version 2 `onUpgrade()` behavior in `DatabaseHelper.java` with a preserving migration from schema version 1 to schema version 2.
- Preserved existing `users` rows during migration by copying `_id` and normalized `username` into the new version 2 table.
- Converted version 1 plain-text passwords into salted PBKDF2 hashes during migration.
- Removed the legacy plain-text password column by rebuilding the user table rather than keeping the old column in place.
- Preserved existing `events` rows during migration by copying `_id`, `user_id`, title, date, time, notes, and SMS reminder values into the version 2 table.
- Added the `event_timestamp` column during migration and set migrated legacy rows to `Event.NO_TIMESTAMP` when the original record did not contain a computable timestamp.
- Added package-visible test constructors so repository and migration behavior can be tested with isolated database files.
- Added `PasswordHasherTest.java` for password hashing and salt behavior.
- Added `EventRepositoryOwnershipInstrumentedTest.java` for user-scoped update/delete behavior.
- Added `DatabaseMigrationInstrumentedTest.java` for schema migration and data-preservation behavior.

### Final verification plan

Run the following in Android Studio before the final ePortfolio upload:

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew assembleDebug
```

Manual workflow test:

1. Create a new account.
2. Log in with the new account.
3. Add an event with date, time, notes, and reminder state.
4. Confirm the dashboard displays the event.
5. Edit the event and confirm the update is visible.
6. Delete the event and confirm it is removed.
7. Create or preserve another user and confirm events remain scoped to the authenticated user.
8. Verify that an upgraded version 1 database keeps existing users and events after opening the version 2 app.

### Final test files

- `app/src/test/java/com/snhu/eventtracker/PasswordHasherTest.java`
- `app/src/test/java/com/snhu/eventtracker/InputValidatorTest.java`
- `app/src/androidTest/java/com/snhu/eventtracker/EventRepositoryOwnershipInstrumentedTest.java`
- `app/src/androidTest/java/com/snhu/eventtracker/DatabaseMigrationInstrumentedTest.java`
