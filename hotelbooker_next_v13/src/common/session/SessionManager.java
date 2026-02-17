package common.session;

import common.model.User;

/**
 * Very small in-memory session for the desktop app.
 */
public class SessionManager {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void login(User u) {
        currentUser = u;
    }

    public static void logout() {
        currentUser = null;
    }
}
