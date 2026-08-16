# CS 360 Event Tracking App - Final ePortfolio Polish Notes

## Artifact

**Course:** CS 360 Mobile Architecture and Programming  
**Category:** Software Design and Engineering  
**Artifact:** Event Tracking Android App

## Instructor feedback addressed

The Milestone Two feedback requested three final ePortfolio improvements:

1. Replace the destructive version 2 upgrade with a migration that preserves existing users and events.
2. Add tests for password hashing, ownership checks, and schema migration.
3. Complete Android Studio build and manual workflow testing when tooling is available.

## Changes completed

- `DatabaseHelper.java` now performs a non-destructive version 1 to version 2 migration.
- Existing user IDs and usernames are preserved.
- Version 1 plain-text passwords are converted to salted PBKDF2 hashes.
- The legacy password column is removed by rebuilding the users table.
- Existing event IDs, owners, titles, dates, times, notes, and SMS flags are preserved.
- `event_timestamp` is added for the enhanced schema.
- `PasswordHasherTest.java` verifies salt generation and hash comparison.
- `EventRepositoryOwnershipInstrumentedTest.java` verifies that one user cannot update or delete another user's event.
- `DatabaseMigrationInstrumentedTest.java` verifies schema migration and data preservation.

## Remaining local verification

This project should be opened in Android Studio for final verification because this container cannot download the Gradle distribution.

Recommended commands:

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew assembleDebug
```

Recommended manual test path:

1. Create account.
2. Log in.
3. Add event.
4. Edit event.
5. Delete event.
6. Confirm user-scoped event visibility.
7. Confirm timestamp-based event ordering.
8. Confirm migration preserves existing version 1 data.
