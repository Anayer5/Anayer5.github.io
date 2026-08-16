# CS 499 Final Artifact 1 - Software Design and Engineering

## Artifact

**Course:** CS 360 Mobile Architecture and Programming  
**Artifact:** Event Tracking Android App  
**Category:** Software Design and Engineering

## Package contents

- `original_project/` contains the original CS 360 Event Tracking Android App code.
- `enhanced_project/` contains the final polished CS 499 enhanced version.
- `evidence/BUILD_VERIFICATION.txt` documents the Android Studio verification plan.
- `evidence/FINAL_POLISH_NOTES.md` documents how instructor feedback was addressed.

## Final polish focus

The final ePortfolio polish addresses the Milestone Two instructor feedback by replacing the destructive schema upgrade with a preserving migration, adding tests for password hashing, user-scoped event ownership, and schema migration, and documenting the remaining local Android Studio verification steps.

## Most relevant enhanced files

- `enhanced_project/app/src/main/java/com/snhu/eventtracker/DatabaseHelper.java`
- `enhanced_project/app/src/main/java/com/snhu/eventtracker/PasswordHasher.java`
- `enhanced_project/app/src/main/java/com/snhu/eventtracker/InputValidator.java`
- `enhanced_project/app/src/main/java/com/snhu/eventtracker/UserRepository.java`
- `enhanced_project/app/src/main/java/com/snhu/eventtracker/EventRepository.java`
- `enhanced_project/app/src/main/java/com/snhu/eventtracker/Event.java`
- `enhanced_project/app/src/test/java/com/snhu/eventtracker/PasswordHasherTest.java`
- `enhanced_project/app/src/test/java/com/snhu/eventtracker/InputValidatorTest.java`
- `enhanced_project/app/src/androidTest/java/com/snhu/eventtracker/EventRepositoryOwnershipInstrumentedTest.java`
- `enhanced_project/app/src/androidTest/java/com/snhu/eventtracker/DatabaseMigrationInstrumentedTest.java`
