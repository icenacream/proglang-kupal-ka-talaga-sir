package common.filehandler;

import common.model.Room;
import common.model.Booking;
import common.model.Payment;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionFileHandler {
    private static final String DATA_DIR = "data";
    private static final String MASTER_DIR = DATA_DIR + File.separator + "master";
    private static final String ROOMS_FILE = MASTER_DIR + File.separator + "rooms.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + File.separator + "bookings.txt";
    private static final String PAYMENTS_FILE = DATA_DIR + File.separator + "payments.txt";

    static {
        // Create directories if they don't exist
        try {
            Files.createDirectories(Paths.get(MASTER_DIR));
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }

    // =============== ROOM OPERATIONS ===============
    public static List<Room> readRoomsFromFile() {
        List<Room> rooms = new ArrayList<>();
        File file = new File(ROOMS_FILE);

        if (!file.exists()) {
            System.out.println("Rooms file not found. Creating with sample data...");
            createSampleRoomsFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ROOMS_FILE))) {
            String line;
            int id = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                Room room = parseRoomLine(line, "R" + id);
                if (room != null) {
                    rooms.add(room);
                    id++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading rooms file: " + e.getMessage());
        }

        return rooms;
    }

    private static Room parseRoomLine(String line, String fallbackRoomId) {
        // Supported schemas:
        // 1) (legacy) hotel|location|price|rating|reviews|amenities|capacity|available
        // 2) (v11) id|hotel|location|price|rating|reviews|amenities|capacity|available|imagePath
        // 3) (v12) id|hotel|location|price|rating|reviews|amenities|capacity|units|available|imagePath
        // 4) (v13) id|hotel|roomType|location|price|rating|reviews|amenities|capacity|units|available|imagePath
        String[] parts = line.split("\\|");
        if (parts.length < 7) return null;

        try {
            int idx = 0;
            String id;
            // If the first column looks like an id (e.g. R1), use it.
            if (parts[0].trim().matches("[A-Za-z]+\\d+")) {
                id = parts[0].trim();
                idx = 1;
            } else {
                id = fallbackRoomId;
            }

            String hotelName = parts[idx++].trim();

            // v13 introduces roomType as the next column after hotelName.
            String roomType = "Standard Room";
            if (parts.length >= idx + 10) {
                roomType = parts[idx++].trim();
            }

            String location = parts[idx++].trim();
            double price = Double.parseDouble(parts[idx++].trim());
            double rating = Double.parseDouble(parts[idx++].trim());
            int reviews = Integer.parseInt(parts[idx++].trim());
            String[] amenities = parts[idx++].trim().split(",");
            int capacity = Integer.parseInt(parts[idx++].trim());

            int units = 1;
            boolean available = true;
            String imagePath = "";

            // v12 has an extra "units" column before the available flag.
            if (parts.length >= idx + 3) {
                // Try parse units; if it fails, fall back to v11 behavior.
                try {
                    units = Integer.parseInt(parts[idx].trim());
                    idx++;
                } catch (Exception ignore) {
                    units = 1;
                }
            }

            // available flag
            if (parts.length > idx) {
                available = Boolean.parseBoolean(parts[idx++].trim());
            }
            // image path
            if (parts.length > idx) {
                imagePath = parts[idx].trim();
            }

            if (imagePath == null || imagePath.isBlank()) {
                imagePath = "assets/images/city_center.jpg";
            }

            for (int i = 0; i < amenities.length; i++) {
                amenities[i] = amenities[i].trim();
            }

            return new Room(id, hotelName, roomType, location, price, rating, reviews, amenities, capacity, units, available, imagePath);
        } catch (Exception e) {
            System.err.println("Error parsing room line: " + line + " - " + e.getMessage());
            return null;
        }
    }

    private static void createSampleRoomsFile() {
        String[] sampleRooms = {
            "R1|Grand Plaza Hotel|Deluxe King|New York, USA|299|4.8|324|Free WiFi,Pool,Spa|2|5|true|assets/images/grand_plaza.jpg",
            "R2|Urban Luxury Suites|Executive Suite|Los Angeles, USA|249|4.6|198|Free WiFi,Gym,Bar|2|4|true|assets/images/urban_luxury.jpg",
            "R3|Beachside Paradise Resort|Ocean View|Miami, USA|399|4.9|452|Beach Access,Pool,Spa|2|3|true|assets/images/beachside_paradise.jpg",
            "R4|Boutique Heritage Inn|Heritage Single|San Francisco, USA|189|4.7|276|Free WiFi,Breakfast,Historic Building|1|2|true|assets/images/boutique_heritage.jpg",
            "R5|Mountain Peak Lodge|Chalet Queen|Aspen, USA|349|4.9|389|Ski-in/Ski-out,Hot Tub,Restaurant|2|2|true|assets/images/mountain_peak.jpg",
            "R6|City Center Comfort Hotel|Standard Twin|Chicago, USA|159|4.5|143|Free WiFi,Parking,Pet Friendly|2|6|true|assets/images/city_center.jpg"
        };

        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (String room : sampleRooms) {
                writer.println(room);
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error creating sample rooms file: " + e.getMessage());
        }
    }

    public static void saveRoom(Room room) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE, true))) {
            writer.println(room.toString());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving room: " + e.getMessage());
        }
    }

    /**
     * Update (replace) a room by id.
     * This rewrites the rooms file to keep data consistent.
     */
    public static void updateRoom(Room updated) {
        List<Room> rooms = readRoomsFromFile();
        boolean replaced = false;
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getId().equals(updated.getId())) {
                rooms.set(i, updated);
                replaced = true;
                break;
            }
        }
        if (!replaced) rooms.add(updated);

        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE, false))) {
            for (Room r : rooms) {
                writer.println(r.toString());
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error updating room: " + e.getMessage());
        }
    }

    // =============== BOOKING OPERATIONS ===============
    public static List<Booking> readBookingsFromFile() {
        List<Booking> bookings = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);

        if (!file.exists()) return bookings;

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                Booking booking = parseBookingLine(line);
                if (booking != null) {
                    bookings.add(booking);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading bookings file: " + e.getMessage());
        }

        return bookings;
    }

    private static Booking parseBookingLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 8) return null;

        try {
            String bookingId = parts[0].trim();
            String guestName = parts[1].trim();
            String roomId = parts[2].trim();
            LocalDate checkIn = LocalDate.parse(parts[3].trim());
            LocalDate checkOut = LocalDate.parse(parts[4].trim());
            int guests = Integer.parseInt(parts[5].trim());
            double totalPrice = Double.parseDouble(parts[6].trim());
            String status = parts[7].trim();

            return new Booking(bookingId, guestName, roomId, checkIn, checkOut, guests, totalPrice, status);
        } catch (Exception e) {
            System.err.println("Error parsing booking line: " + line);
            return null;
        }
    }

    public static void saveBooking(Booking booking) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, true))) {
            writer.println(booking.toString());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving booking: " + e.getMessage());
        }
    }

    /**
     * Update (replace) a booking by id.
     * Rewrites the bookings file to keep data consistent.
     */
    public static void updateBooking(Booking updated) {
        List<Booking> bookings = readBookingsFromFile();
        boolean replaced = false;
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId().equalsIgnoreCase(updated.getBookingId())) {
                bookings.set(i, updated);
                replaced = true;
                break;
            }
        }
        if (!replaced) bookings.add(updated);

        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, false))) {
            for (Booking b : bookings) {
                writer.println(b.toString());
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error updating booking: " + e.getMessage());
        }
    }

    // =============== PAYMENT OPERATIONS ===============
    public static List<Payment> readPaymentsFromFile() {
        List<Payment> payments = new ArrayList<>();
        File file = new File(PAYMENTS_FILE);

        if (!file.exists()) return payments;

        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                Payment payment = parsePaymentLine(line);
                if (payment != null) {
                    payments.add(payment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading payments file: " + e.getMessage());
        }

        return payments;
    }

    private static Payment parsePaymentLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 6) return null;

        try {
            String paymentId = parts[0].trim();
            String bookingId = parts[1].trim();
            double amount = Double.parseDouble(parts[2].trim());
            String paymentMethod = parts[3].trim();
            String status = parts[4].trim();
            LocalDateTime paymentDate = LocalDateTime.parse(parts[5].trim());

            return new Payment(paymentId, bookingId, amount, paymentMethod, status, paymentDate);
        } catch (Exception e) {
            System.err.println("Error parsing payment line: " + line);
            return null;
        }
    }

    public static void savePayment(Payment payment) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PAYMENTS_FILE, true))) {
            writer.println(payment.toString());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving payment: " + e.getMessage());
        }
    }

    /**
     * Updates the latest payment record for a booking (by paymentDate) to match the new amount.
     * If no payment exists, this does nothing.
     */
    public static void updateLatestPaymentAmountForBooking(String bookingId, double newAmount) {
        if (bookingId == null || bookingId.isBlank()) return;

        List<Payment> payments = readPaymentsFromFile();
        if (payments.isEmpty()) return;

        Payment latest = null;
        for (Payment p : payments) {
            if (p == null) continue;
            if (!bookingId.equalsIgnoreCase(p.getBookingId())) continue;
            if (latest == null) {
                latest = p;
            } else {
                try {
                    if (p.getPaymentDate() != null && latest.getPaymentDate() != null && p.getPaymentDate().isAfter(latest.getPaymentDate())) {
                        latest = p;
                    }
                } catch (Exception ignore) {}
            }
        }

        if (latest == null) return;
        latest.setAmount(newAmount);

        try (PrintWriter writer = new PrintWriter(new FileWriter(PAYMENTS_FILE, false))) {
            for (Payment p : payments) {
                writer.println(p.toString());
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error updating payment: " + e.getMessage());
        }
    }
}
