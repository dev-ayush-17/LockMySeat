package util;

import model.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager demonstrates Collections and Thread-Safety (ConcurrentHashMap).
 * Manages in-memory authorization tokens without needing external JWT libraries.
 */
public class SessionManager {
    // Map<Token, User>
    private static final Map<String, User> sessions = new ConcurrentHashMap<>();

    public static String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public static User getUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return sessions.get(token.trim());
    }

    public static void removeSession(String token) {
        if (token != null) {
            sessions.remove(token.trim());
        }
    }
}
