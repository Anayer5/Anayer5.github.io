package com.snhu.eventtracker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for salted PBKDF2 password hashing. */
public class PasswordHasherTest {

    @Test
    public void generateSalt_returnsUniqueBase64Values() {
        String firstSalt = PasswordHasher.generateSalt();
        String secondSalt = PasswordHasher.generateSalt();

        assertNotNull(firstSalt);
        assertNotNull(secondSalt);
        assertNotEquals(firstSalt, secondSalt);
    }

    @Test
    public void hashPassword_samePasswordAndSalt_returnsSameHash() {
        String salt = PasswordHasher.generateSalt();
        String firstHash = PasswordHasher.hashPassword("StrongPass123", salt);
        String secondHash = PasswordHasher.hashPassword("StrongPass123", salt);

        assertTrue(PasswordHasher.hashesMatch(firstHash, secondHash));
    }

    @Test
    public void hashPassword_differentPassword_returnsDifferentHash() {
        String salt = PasswordHasher.generateSalt();
        String correctHash = PasswordHasher.hashPassword("StrongPass123", salt);
        String wrongHash = PasswordHasher.hashPassword("WrongPass123", salt);

        assertFalse(PasswordHasher.hashesMatch(correctHash, wrongHash));
    }
}
