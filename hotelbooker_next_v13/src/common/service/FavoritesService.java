package common.service;

import common.model.User;

import java.nio.file.*;
import java.util.*;

/**
 * Favorites persisted as: email|roomId
 */
public final class FavoritesService {
    private static final String PATH = "data/favorites.txt";

    private FavoritesService() {}

    private static String key(User u) {
        if (u == null) return null;
        String email = u.getEmail();
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public static synchronized Set<String> getFavorites(User u) {
        String k = key(u);
        if (k == null) return Set.of();
        Set<String> out = new HashSet<>();
        try {
            Path p = Paths.get(PATH);
            if (!Files.exists(p)) return Set.of();
            for (String line : Files.readAllLines(p)) {
                if (line == null) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;
                if (k.equalsIgnoreCase(parts[0].trim())) {
                    out.add(parts[1].trim());
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static synchronized boolean isFavorite(User u, String roomId) {
        if (roomId == null) return false;
        return getFavorites(u).contains(roomId.trim());
    }

    public static synchronized boolean toggle(User u, String roomId) {
        String k = key(u);
        if (k == null || roomId == null || roomId.isBlank()) return false;
        roomId = roomId.trim();

        try {
            Files.createDirectories(Paths.get("data"));
            Path p = Paths.get(PATH);
            List<String> lines = Files.exists(p) ? Files.readAllLines(p) : new ArrayList<>();
            String entry = k + "|" + roomId;
            boolean removed = lines.removeIf(s -> s != null && s.trim().equalsIgnoreCase(entry));
            if (!removed) lines.add(entry);
            Files.write(p, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return !removed;
        } catch (Exception ignored) {
            return false;
        }
    }
}
