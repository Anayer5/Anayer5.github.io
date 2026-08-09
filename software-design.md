# Enhancement One: Software Design and Engineering

## Artifact Overview

**Artifact:** CS 360 Event Tracking Android App  
**Course:** CS 360: Mobile Architecture and Programming  
**Category:** Software Design and Engineering

The original Event Tracking Android App allowed users to create accounts, log in, create events, edit events, delete events, and store event data locally with SQLite.

## Enhancement Summary

The enhanced version improves the original mobile app through stronger architecture, centralized validation, PBKDF2 password hashing, user-scoped event operations, timestamp-based event storage, and a preserving database migration.

## Original Artifact

- [Original CS 360 Project Files](artifacts/cs360/original/)

## Enhanced Artifact

- [Enhanced CS 360 Project Files](artifacts/cs360/enhanced/)
- [Final CS 360 Artifact ZIP](artifacts/cs360/CS499_Final_Artifact_1_CS360_EventTracking_Polished.zip)

## Evidence and Testing

- [Final Polish Notes](artifacts/cs360/evidence/FINAL_POLISH_NOTES.md)
- [Build Verification Notes](artifacts/cs360/evidence/BUILD_VERIFICATION.txt)

## Key Enhanced Files

- `DatabaseHelper.java`
- `UserRepository.java`
- `EventRepository.java`
- `PasswordHasher.java`
- `InputValidator.java`
- `Event.java`
- `PasswordHasherTest.java`
- `DatabaseMigrationInstrumentedTest.java`
- `EventRepositoryOwnershipInstrumentedTest.java`

## Narrative

- [Download Enhancement One Narrative](narratives/CS360_Enhancement_One_Narrative.docx)

[Back to Home](index.md)
