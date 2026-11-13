package com.smarterevententrysystem.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Login attempt limit: after MAX_ATTEMPTS wrong passwords for a username,
 * that username is locked for LOCK_SECONDS. Counters live in memory
 * (they reset when the application is closed).
 */
public class LoginAttemptTracker {

    public static final int MAX_ATTEMPTS = 3;
    public static final int LOCK_SECONDS = 30;

    private static final Map<String, Integer> failures = new HashMap<>();
    private static final Map<String, Long> lockedUntil = new HashMap<>();

    private LoginAttemptTracker() { }

    /** Seconds still to wait (0 = not locked). */
    public static synchronized long secondsLeft(String username) {
        String key = normalize(username);
        Long until = lockedUntil.get(key);
        if (until == null) {
            return 0;
        }
        long millisLeft = until - System.currentTimeMillis();
        if (millisLeft <= 0) {
            lockedUntil.remove(key);
            failures.remove(key);
            return 0;
        }
        return (millisLeft + 999) / 1000;
    }

    /** Records a wrong password. Returns the attempts left (0 = locked now). */
    public static synchronized int recordFailure(String username) {
        String key = normalize(username);
        int count = failures.merge(key, 1, Integer::sum);
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(key, System.currentTimeMillis() + LOCK_SECONDS * 1000L);
            failures.remove(key);
            return 0;
        }
        return MAX_ATTEMPTS - count;
    }

    public static synchronized void reset(String username) {
        String key = normalize(username);
        failures.remove(key);
        lockedUntil.remove(key);
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
