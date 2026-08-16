package com.snhu.eventtracker;

/**
 * Centralizes user input rules so Activities do not duplicate validation logic.
 */
public final class InputValidator {

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_EVENT_TITLE_LENGTH = 80;

    private InputValidator() {
        // Utility class; no instances.
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    public static boolean isUsernameValid(String username) {
        String normalized = normalizeUsername(username);
        return normalized.length() >= MIN_USERNAME_LENGTH && normalized.matches("[a-z0-9._-]+?");
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isEventTitleValid(String title) {
        if (title == null) {
            return false;
        }
        String trimmed = title.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_EVENT_TITLE_LENGTH;
    }
}
