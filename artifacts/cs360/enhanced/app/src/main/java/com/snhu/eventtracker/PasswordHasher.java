package com.snhu.eventtracker;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Security helper used by UserRepository to avoid storing plain-text passwords.
 *
 * <p>The database stores only a random salt and a PBKDF2-derived password hash.
 * During login, the entered password is hashed with the stored salt and compared
 * with the stored hash.</p>
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordHasher() {
        // Utility class; no instances.
    }

    /** Creates a cryptographically random salt encoded as Base64 text. */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return java.util.Base64.getEncoder().encodeToString(salt);
    }

    /** Returns a Base64 PBKDF2 hash for the given password and Base64 salt. */
    public static String hashPassword(String password, String base64Salt) {
        try {
            byte[] salt = java.util.Base64.getDecoder().decode(base64Salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            byte[] hash = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is unavailable on this device.", e);
        }
    }

    /** Performs a constant-time comparison of two hash strings. */
    public static boolean hashesMatch(String expectedHash, String actualHash) {
        if (expectedHash == null || actualHash == null) {
            return false;
        }
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = actualHash.getBytes(StandardCharsets.UTF_8);
        int diff = expected.length ^ actual.length;
        for (int i = 0; i < expected.length && i < actual.length; i++) {
            diff |= expected[i] ^ actual[i];
        }
        return diff == 0;
    }
}
