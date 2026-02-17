package common.service;

import common.model.User;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * File-based user account service.
 * File: data/users.txt
 * Format: userId|fullName|email|password|createdAt
 */
public class UserService {
    private static final String USERS_FILE = "data/users.txt";

    private static UserService instance;

    public static synchronized UserService getInstance() {
        if (instance == null) instance = new UserService();
        return instance;
    }

    private UserService() {
        ensureFile();
    }

    private void ensureFile() {
        try {
            Files.createDirectories(Paths.get("data"));
            if (!Files.exists(Paths.get(USERS_FILE))) {
                Files.write(Paths.get(USERS_FILE), Collections.singletonList("# userId|fullName|email|password|createdAt"));
            }
        } catch (IOException ignored) {}
    }

    public synchronized List<User> getAllUsers() {
        ensureFile();
        List<User> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                User u = parse(line);
                if (u != null) out.add(u);
            }
        } catch (IOException ignored) {}
        return out;
    }

    public synchronized User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        String e = email.trim().toLowerCase(Locale.ROOT);
        for (User u : getAllUsers()) {
            if (u.getEmail().toLowerCase(Locale.ROOT).equals(e) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public synchronized boolean emailExists(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase(Locale.ROOT);
        for (User u : getAllUsers()) {
            if (u.getEmail().toLowerCase(Locale.ROOT).equals(e)) return true;
        }
        return false;
    }

    public synchronized User register(String fullName, String email, String password) {
        ensureFile();
        if (fullName == null || fullName.trim().isEmpty()) return null;
        if (email == null || email.trim().isEmpty()) return null;
        if (password == null || password.isEmpty()) return null;
        if (emailExists(email)) return null;

        String userId = "U" + System.currentTimeMillis();
        User u = new User(userId, fullName.trim(), email.trim(), password, LocalDateTime.now());

        try {
            Files.write(Paths.get(USERS_FILE), Collections.singletonList(u.toString()), StandardOpenOption.APPEND);
            return u;
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized boolean updateProfile(String userId, String newFullName, String newPasswordOrNull) {
        ensureFile();
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(USERS_FILE));
        } catch (IOException e) {
            return false;
        }

        boolean updated = false;
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                out.add(line);
                continue;
            }
            User u = parse(t);
            if (u != null && u.getUserId().equals(userId)) {
                if (newFullName != null && !newFullName.trim().isEmpty()) u.setFullName(newFullName.trim());
                if (newPasswordOrNull != null && !newPasswordOrNull.isEmpty()) u.setPassword(newPasswordOrNull);
                out.add(u.toString());
                updated = true;
            } else {
                out.add(line);
            }
        }

        if (!updated) return false;
        try {
            Files.write(Paths.get(USERS_FILE), out);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized boolean deleteUser(String userId) {
        ensureFile();
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(USERS_FILE));
        } catch (IOException e) {
            return false;
        }
        boolean removed = false;
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                out.add(line);
                continue;
            }
            User u = parse(t);
            if (u != null && u.getUserId().equals(userId)) {
                removed = true;
                continue;
            }
            out.add(line);
        }
        if (!removed) return false;
        try {
            Files.write(Paths.get(USERS_FILE), out);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private User parse(String line) {
        try {
            String[] p = line.split("\\|");
            if (p.length < 5) return null;
            return new User(p[0], p[1], p[2], p[3], LocalDateTime.parse(p[4]));
        } catch (Exception e) {
            return null;
        }
    }
}
