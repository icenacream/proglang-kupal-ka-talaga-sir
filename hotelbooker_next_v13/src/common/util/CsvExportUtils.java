package common.util;

import common.model.Booking;
import common.model.Room;
import common.model.User;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Simple CSV exports for Admin.
 * Files are saved under ./exports
 */
public class CsvExportUtils {
    private static final String EXPORT_DIR = "exports";

    private static String esc(String v) {
        if (v == null) return "";
        String s = v.replace("\r", " ").replace("\n", " ");
        boolean needs = s.contains(",") || s.contains("\"");
        s = s.replace("\"", "\"\"");
        return needs ? ("\"" + s + "\"") : s;
    }

    public static Path exportBookings(List<Booking> bookings, List<Room> rooms) throws Exception {
        Files.createDirectories(Paths.get(EXPORT_DIR));
        String name = "bookings_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        Path out = Paths.get(EXPORT_DIR + File.separator + name);

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("booking_id,guest,room_id,hotel,location,check_in,check_out,guests,total,status");
            w.newLine();
            for (Booking b : bookings) {
                if (b == null) continue;
                Room r = rooms == null ? null : rooms.stream().filter(x -> x != null && x.getId().equalsIgnoreCase(b.getRoomId())).findFirst().orElse(null);
                String hotel = r != null ? r.getHotelName() : "";
                String loc = r != null ? r.getLocation() : "";
                w.write(
                        esc(b.getBookingId()) + "," +
                        esc(b.getGuestName()) + "," +
                        esc(b.getRoomId()) + "," +
                        esc(hotel) + "," +
                        esc(loc) + "," +
                        esc(b.getCheckInDate() != null ? b.getCheckInDate().toString() : "") + "," +
                        esc(b.getCheckOutDate() != null ? b.getCheckOutDate().toString() : "") + "," +
                        b.getNumberOfGuests() + "," +
                        b.getTotalPrice() + "," +
                        esc(b.getStatus())
                );
                w.newLine();
            }
            w.flush();
        }
        return out;
    }

    public static Path exportGuests(List<User> users) throws Exception {
        Files.createDirectories(Paths.get(EXPORT_DIR));
        String name = "guests_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        Path out = Paths.get(EXPORT_DIR + File.separator + name);

        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("user_id,name,email,created_at");
            w.newLine();
            for (User u : users) {
                if (u == null) continue;
                String created = u.getCreatedAt() == null ? "" : u.getCreatedAt().toString();
                w.write(
                        esc(u.getUserId()) + "," +
                        esc(u.getFullName()) + "," +
                        esc(u.getEmail()) + "," +
                        esc(created)
                );
                w.newLine();
            }
            w.flush();
        }
        return out;
    }
}
