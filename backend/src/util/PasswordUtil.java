package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil provides secure password hashing using SHA-256 and cryptographic salts.
 * No external BCrypt library required. Demonstrates core Java security primitives.
 */
public class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a 16-byte random salt encoded as Base64.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes password + salt using standard SHA-256 MessageDigest.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in this JVM", e);
        }
    }

    /**
     * Verifies if input password matches the stored hash for given salt.
     */
    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        String hash = hashPassword(password, salt);
        return hash.equals(expectedHash);
    }
}
