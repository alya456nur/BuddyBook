package com.example.buddybook;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtils {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtils() {
        // Prevent this utility class from being instantiated.
    }

    /**
     * Hashes a password using PBKDF2 with a randomly generated salt.
     *
     * Stored format:
     * iterations:salt:hash
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        byte[] salt = generateSalt();
        byte[] hash = generateHash(password.toCharArray(), salt, ITERATIONS);

        String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
        String encodedHash = Base64.encodeToString(hash, Base64.NO_WRAP);

        return ITERATIONS + ":" + encodedSalt + ":" + encodedHash;
    }

    /**
     * Checks a plain password against the saved PBKDF2 password.
     */
    public static boolean verifyPassword(
            String enteredPassword,
            String storedPassword
    ) {
        if (enteredPassword == null ||
                storedPassword == null ||
                storedPassword.trim().isEmpty()) {
            return false;
        }

        try {
            String[] parts = storedPassword.split(":");

            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);

            byte[] salt = Base64.decode(
                    parts[1],
                    Base64.NO_WRAP
            );

            byte[] expectedHash = Base64.decode(
                    parts[2],
                    Base64.NO_WRAP
            );

            byte[] enteredHash = generateHash(
                    enteredPassword.toCharArray(),
                    salt,
                    iterations
            );

            return slowEquals(expectedHash, enteredHash);

        } catch (Exception exception) {
            return false;
        }
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return salt;
    }

    private static byte[] generateHash(
            char[] password,
            byte[] salt,
            int iterations
    ) {
        PBEKeySpec specification = new PBEKeySpec(
                password,
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory;

            try {
                factory = SecretKeyFactory.getInstance(
                        "PBKDF2WithHmacSHA256"
                );
            } catch (NoSuchAlgorithmException exception) {
                // Fallback for older Android versions.
                factory = SecretKeyFactory.getInstance(
                        "PBKDF2WithHmacSHA1"
                );
            }

            return factory.generateSecret(specification).getEncoded();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to hash password",
                    exception
            );

        } finally {
            specification.clearPassword();
        }
    }

    /**
     * Constant-time comparison to reduce timing attacks.
     */
    private static boolean slowEquals(
            byte[] expected,
            byte[] actual
    ) {
        if (expected == null || actual == null) {
            return false;
        }

        int difference = expected.length ^ actual.length;

        int minimumLength = Math.min(
                expected.length,
                actual.length
        );

        for (int i = 0; i < minimumLength; i++) {
            difference |= expected[i] ^ actual[i];
        }

        return difference == 0;
    }
}