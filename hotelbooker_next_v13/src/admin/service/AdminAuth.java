package admin.service;

import java.nio.file.*;
import java.util.*;

/**
 * File-based admin/staff authentication.
 *
 * Data file: data/staff.txt
 * Format: username|password|role
 * Roles: ADMIN, STAFF
 *
 * Defaults (auto-seeded if file missing):
 *  - admin|0000|ADMIN
 *  - staff|1234|STAFF
 */
public class AdminAuth {
    public enum Role { ADMIN, STAFF }

    private static AdminAuth instance;

    private static final String STAFF_PATH = "data/staff.txt";

    private boolean loggedIn = false;
    private String currentUser = null;
    private Role currentRole = null;

    private AdminAuth() {}

    public static synchronized AdminAuth getInstance() {
        if (instance == null) instance = new AdminAuth();
        return instance;
    }

    public synchronized void ensureSeed() {
        try {
            Path p = Paths.get(STAFF_PATH);
            if (!Files.exists(p)) {
                Files.createDirectories(p.getParent());
                String seed = "admin|0000|ADMIN\nstaff|1234|STAFF\n";
                Files.writeString(p, seed, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (Exception ignored) {}
    }

    private synchronized List<String[]> readStaffRows() {
        ensureSeed();
        List<String[]> rows = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(STAFF_PATH));
            for (String line : lines) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 3) continue;
                String u = parts[0].trim();
                String pw = parts[1].trim();
                String role = parts[2].trim().toUpperCase();
                if (u.isEmpty() || pw.isEmpty() || role.isEmpty()) continue;
                rows.add(new String[]{u, pw, role});
            }
        } catch (Exception ignored) {}
        return rows;
    }

    public synchronized boolean login(String username, char[] password) {
        if (username == null || password == null) return false;
        String u = username.trim();
        if (u.isEmpty()) return false;

        String pw = new String(password);

        for (String[] row : readStaffRows()) {
            if (!u.equals(row[0])) continue;
            if (!pw.equals(row[1])) return false;

            loggedIn = true;
            currentUser = row[0];
            try {
                currentRole = Role.valueOf(row[2]);
            } catch (Exception ex) {
                currentRole = Role.STAFF;
            }
            return true;
        }
        return false;
    }

    public synchronized void logout() {
        loggedIn = false;
        currentUser = null;
        currentRole = null;
    }

    public synchronized boolean isLoggedIn() { return loggedIn; }

    public synchronized String getCurrentUser() { return currentUser; }

    public synchronized Role getCurrentRole() { return currentRole == null ? Role.STAFF : currentRole; }

    public synchronized boolean isAdmin() { return isLoggedIn() && getCurrentRole() == Role.ADMIN; }
}
