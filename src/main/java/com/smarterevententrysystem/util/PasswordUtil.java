package com.smarterevententrysystem.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing.
 * New passwords use BCrypt (salted and deliberately slow, so guessing is hard).
 * Old passwords stored as plain SHA-256 (from the first version of the project)
 * are still accepted once and then upgraded to BCrypt automatically.
 */
public class PasswordUtil {

    private PasswordUtil() { }

    /** Creates a BCrypt hash (60 characters, starts with $2a$). */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /** Checks a typed password against the stored hash (BCrypt or old SHA-256). */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        if (storedHash.startsWith("$2")) {
            try {
                return BCrypt.checkpw(plainPassword, storedHash);
            } catch (IllegalArgumentException e) {
                return false;   // stored value is not a valid BCrypt hash
            }
        }
        byte[] typed = sha256(plainPassword).getBytes(StandardCharsets.UTF_8);
        byte[] stored = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(typed, stored);
    }

    /** True if the stored hash is the old SHA-256 type that should be upgraded. */
    public static boolean isLegacyHash(String storedHash) {
        return storedHash != null && !storedHash.startsWith("$2");
    }

    private static String sha256(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this computer", e);
        }
    }
}
