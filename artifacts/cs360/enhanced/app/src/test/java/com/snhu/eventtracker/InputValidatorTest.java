package com.snhu.eventtracker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Unit tests for validation rules added during the CS 499 software-design enhancement. */
public class InputValidatorTest {

    @Test
    public void normalizeUsername_trimsAndLowercasesInput() {
        assertEquals("asher.nayer", InputValidator.normalizeUsername("  Asher.Nayer  "));
    }

    @Test
    public void isUsernameValid_rejectsShortOrUnsafeNames() {
        assertFalse(InputValidator.isUsernameValid("ab"));
        assertFalse(InputValidator.isUsernameValid("asher nayer"));
        assertTrue(InputValidator.isUsernameValid("asher_nayer-01"));
    }

    @Test
    public void isPasswordValid_requiresEightCharacters() {
        assertFalse(InputValidator.isPasswordValid("short"));
        assertTrue(InputValidator.isPasswordValid("longPass1"));
    }

    @Test
    public void isEventTitleValid_rejectsEmptyAndOverlongTitles() {
        assertFalse(InputValidator.isEventTitleValid("   "));
        assertTrue(InputValidator.isEventTitleValid("Capstone code review"));
    }
}
