package common.service;

import common.model.Review;
import common.util.FileUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Simple file-backed reviews.
 * Format: roomId|userEmail|rating|comment|date
 */
public class ReviewService {
    private static final Path FILE = Path.of("data", "reviews.txt");

    public static List<Review> getReviewsForRoom(String roomId) {
        List<String> lines = FileUtils.readAllLinesSafe(FILE);
        List<Review> out = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 5) continue;
            if (!p[0].equals(roomId)) continue;
            try {
                out.add(new Review(p[0], p[1], Integer.parseInt(p[2]), p[3], LocalDate.parse(p[4])));
            } catch (Exception ignore) {
            }
        }
        out.sort(Comparator.comparing(Review::getDate).reversed());
        return out;
    }

    /**
     * Returns all reviews across rooms.
     */
    public static List<Review> getAllReviews() {
        List<String> lines = FileUtils.readAllLinesSafe(FILE);
        List<Review> out = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 5) continue;
            try {
                out.add(new Review(p[0], p[1], Integer.parseInt(p[2]), p[3], LocalDate.parse(p[4])));
            } catch (Exception ignore) {
            }
        }
        out.sort(Comparator.comparing(Review::getDate).reversed());
        return out;
    }

    /**
     * Deletes a review (1 per user per room).
     */
    public static boolean delete(String roomId, String userEmail) {
        if (roomId == null || roomId.isBlank() || userEmail == null || userEmail.isBlank()) return false;
        List<String> lines = FileUtils.readAllLinesSafe(FILE);
        List<String> out = new ArrayList<>();
        boolean removed = false;
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length >= 2 && p[0].equals(roomId) && p[1].equalsIgnoreCase(userEmail)) {
                removed = true;
                continue;
            }
            out.add(line);
        }
        if (removed) FileUtils.writeAllLines(FILE, out);
        return removed;
    }

    public static Stats getStatsForRoom(String roomId) {
        List<Review> reviews = getReviewsForRoom(roomId);
        if (reviews.isEmpty()) return new Stats(0, 0);
        double sum = 0;
        for (Review r : reviews) sum += r.getRating();
        return new Stats(sum / reviews.size(), reviews.size());
    }

    /**
     * Upserts one review per user per room.
     */
    public static void upsert(String roomId, String userEmail, int rating, String comment) {
        FileUtils.ensureParentDir(FILE);
        List<String> lines = FileUtils.readAllLinesSafe(FILE);
        List<String> out = new ArrayList<>();
        boolean replaced = false;

        String safeComment = (comment == null ? "" : comment)
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();

        String newLine = String.join("|",
                roomId,
                userEmail,
                String.valueOf(Math.max(1, Math.min(5, rating))),
                safeComment,
                LocalDate.now().toString()
        );

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length >= 2 && p[0].equals(roomId) && p[1].equalsIgnoreCase(userEmail)) {
                out.add(newLine);
                replaced = true;
            } else {
                out.add(line);
            }
        }
        if (!replaced) out.add(newLine);
        FileUtils.writeAllLines(FILE, out);
    }

    public record Stats(double avg, int count) {
        public String tag() {
            if (count <= 0) return "New";
            return String.format("%.1f", avg);
        }
    }
}
