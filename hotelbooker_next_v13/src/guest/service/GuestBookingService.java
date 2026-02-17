package guest.service;

import common.model.Room;
import common.model.Booking;
import common.model.Payment;
import common.filehandler.TransactionFileHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class GuestBookingService {

    private static final ThreadLocal<String> LAST_ERROR = new ThreadLocal<>();
    
    public GuestBookingService() {
    }

    /**
     * Fetch all available hotels/rooms from the data file
     */
    public List<Room> getAllAvailableRooms() {
        return TransactionFileHandler.readRoomsFromFile()
                .stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Fetch all rooms (available + unavailable).
     */
    public List<Room> getAllRooms() {
        return TransactionFileHandler.readRoomsFromFile();
    }

    /**
     * Search rooms by destination (hotel name or location)
     */
    public List<Room> searchRoomsByDestination(String destination) {
        String searchTerm = destination.toLowerCase().trim();
        
        return TransactionFileHandler.readRoomsFromFile()
                .stream()
                .filter(room -> room.getHotelName().toLowerCase().contains(searchTerm) ||
                               room.getLocation().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Search rooms by price range
     */
    public List<Room> searchRoomsByPrice(double minPrice, double maxPrice) {
        return TransactionFileHandler.readRoomsFromFile()
                .stream()
                .filter(Room::isAvailable)
                .filter(room -> room.getPricePerNight() >= minPrice && 
                               room.getPricePerNight() <= maxPrice)
                .collect(Collectors.toList());
    }

    /**
     * Search rooms by rating
     */
    public List<Room> searchRoomsByRating(double minRating) {
        return TransactionFileHandler.readRoomsFromFile()
                .stream()
                .filter(Room::isAvailable)
                .filter(room -> room.getRating() >= minRating)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific room by ID
     */
    public Room getRoomById(String roomId) {
        return TransactionFileHandler.readRoomsFromFile()
                .stream()
                .filter(room -> room.getId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a room is available
     */
    public boolean isRoomAvailable(String roomId) {
        Room room = getRoomById(roomId);
        return room != null && room.isAvailable();
    }

    /**
     * Calculate total booking price
     */
    public double calculateTotalPrice(String roomId, int numberOfNights) {
        Room room = getRoomById(roomId);
        if (room != null) {
            return room.getPricePerNight() * numberOfNights;
        }
        return 0;
    }

    /**
     * Create a booking + payment, and mark the room as unavailable.
     */
    public Booking createBooking(String guestName, String roomId, LocalDate checkIn, LocalDate checkOut,
                                 int guests, String paymentMethod) {
        return createBooking(guestName, roomId, checkIn, checkOut, guests, paymentMethod, null);
    }

    /**
     * Create a booking with an optional promo code.
     */
    public Booking createBooking(String guestName, String roomId, LocalDate checkIn, LocalDate checkOut,
                                 int guests, String paymentMethod, String promoCode) {
        LAST_ERROR.set(null);
        Room room = getRoomById(roomId);
        if (room == null) {
            LAST_ERROR.set("Room not found.");
            return null;
        }
        // Room 'available' flag is treated as whether the room is listed/enabled by admin.
        if (!room.isAvailable()) {
            LAST_ERROR.set("This room is currently unavailable.");
            return null;
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            LAST_ERROR.set("Invalid dates. Check-out must be after check-in.");
            return null;
        }
        // Date validation: no past check-in
        if (checkIn.isBefore(LocalDate.now())) {
            LAST_ERROR.set("Check-in date cannot be in the past.");
            return null;
        }
        if (guests <= 0 || guests > room.getCapacity()) {
            LAST_ERROR.set("Guest count must be between 1 and " + room.getCapacity() + ".");
            return null;
        }

        // Booking conflict prevention (date overlap)
        if (!isRoomAvailableForDates(roomId, checkIn, checkOut)) {
            LAST_ERROR.set("Room is already booked for the selected dates.");
            return null;
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            LAST_ERROR.set("Invalid stay length.");
            return null;
        }
        double total = room.getPricePerNight() * nights;

        // Promo code (percent discount)
        double pct = common.service.PromoCodeService.getDiscountPercent(promoCode);
        if (pct > 0) {
            total = total * (1.0 - (pct / 100.0));
        }

        String bookingId = "B" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, guestName, roomId, checkIn, checkOut, guests, total, "CONFIRMED");
        TransactionFileHandler.saveBooking(booking);

        String paymentId = "P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment(paymentId, bookingId, total, paymentMethod, "PAID", LocalDateTime.now());
        TransactionFileHandler.savePayment(payment);

        // NOTE: We no longer permanently mark the room as unavailable.
        // Availability is computed from bookings (date overlap prevention).

        return booking;
    }

    /**
     * Checks whether a room has capacity (units) left for the requested date range.
     * Overlap rule: start < existingEnd AND end > existingStart.
     */
    public boolean isRoomAvailableForDates(String roomId, LocalDate start, LocalDate end) {
        if (roomId == null || start == null || end == null) return false;

        // Determine how many units exist for this room listing.
        int units = TransactionFileHandler.readRoomsFromFile().stream()
                .filter(r -> r != null && roomId.equalsIgnoreCase(r.getId()))
                .map(r -> r.getUnits())
                .findFirst()
                .orElse(1);
        units = Math.max(1, units);

        long overlappingConfirmed = TransactionFileHandler.readBookingsFromFile().stream()
                .filter(b -> b != null)
                .filter(b -> roomId.equalsIgnoreCase(b.getRoomId()))
                .filter(b -> b.getStatus() != null && b.getStatus().equalsIgnoreCase("CONFIRMED"))
                .filter(b -> start.isBefore(b.getCheckOutDate()) && end.isAfter(b.getCheckInDate()))
                .count();

        return overlappingConfirmed < units;
    }

    /**
     * Returns how many units are still available for the given date range (CONFIRMED bookings only).
     */
    public int getRemainingUnitsForDates(String roomId, LocalDate start, LocalDate end) {
        if (roomId == null || start == null || end == null) return 0;

        int units = TransactionFileHandler.readRoomsFromFile().stream()
                .filter(r -> r != null && roomId.equalsIgnoreCase(r.getId()))
                .map(r -> r.getUnits())
                .findFirst()
                .orElse(1);
        units = Math.max(1, units);

        long overlappingConfirmed = TransactionFileHandler.readBookingsFromFile().stream()
                .filter(b -> b != null)
                .filter(b -> roomId.equalsIgnoreCase(b.getRoomId()))
                .filter(b -> b.getStatus() != null && b.getStatus().equalsIgnoreCase("CONFIRMED"))
                .filter(b -> start.isBefore(b.getCheckOutDate()) && end.isAfter(b.getCheckInDate()))
                .count();

        int remaining = (int) (units - overlappingConfirmed);
        return Math.max(0, remaining);
    }


    /**
     * Same as isRoomAvailableForDates, but excludes one booking (used for reschedule).
     */
    public boolean isRoomAvailableForDatesExcludingBooking(String roomId, String excludeBookingId, LocalDate start, LocalDate end) {
        if (roomId == null || start == null || end == null) return false;

        int units = TransactionFileHandler.readRoomsFromFile().stream()
                .filter(r -> r != null && roomId.equalsIgnoreCase(r.getId()))
                .map(r -> r.getUnits())
                .findFirst()
                .orElse(1);
        units = Math.max(1, units);

        long overlappingConfirmed = TransactionFileHandler.readBookingsFromFile().stream()
                .filter(b -> b != null)
                .filter(b -> excludeBookingId == null || !excludeBookingId.equalsIgnoreCase(b.getBookingId()))
                .filter(b -> roomId.equalsIgnoreCase(b.getRoomId()))
                .filter(b -> b.getStatus() != null && b.getStatus().equalsIgnoreCase("CONFIRMED"))
                .filter(b -> start.isBefore(b.getCheckOutDate()) && end.isAfter(b.getCheckInDate()))
                .count();

        return overlappingConfirmed < units;
    }

    public String getLastError() {
        String msg = LAST_ERROR.get();
        return (msg == null || msg.isBlank()) ? "Booking failed. Please check your inputs." : msg;
    }

    /**
     * Return bookings for a specific guest name (case-insensitive match).
     */
    public List<Booking> getBookingsByGuestName(String guestName) {
        if (guestName == null) return List.of();
        String q = guestName.trim().toLowerCase();
        if (q.isEmpty()) return List.of();

        return TransactionFileHandler.readBookingsFromFile()
                .stream()
                .filter(b -> b.getGuestName() != null && b.getGuestName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Get a booking by its ID.
     */
    public Booking getBookingById(String bookingId) {
        if (bookingId == null) return null;
        return TransactionFileHandler.readBookingsFromFile()
                .stream()
                .filter(b -> bookingId.equalsIgnoreCase(b.getBookingId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Cancel a booking and make its room available again.
     */
    public boolean cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) return false;

        List<Booking> all = TransactionFileHandler.readBookingsFromFile();
        Booking target = null;
        for (Booking b : all) {
            if (bookingId.equalsIgnoreCase(b.getBookingId())) {
                target = b;
                break;
            }
        }
        if (target == null) return false;

        target.setStatus("CANCELLED");
        TransactionFileHandler.updateBooking(target);
        return true;
    }

    /**
     * Reschedule/edit an existing CONFIRMED booking.
     * Recomputes total price and updates the latest payment amount for this booking.
     */
    public boolean rescheduleBooking(String bookingId, LocalDate newCheckIn, LocalDate newCheckOut, int newGuests) {
        if (bookingId == null || bookingId.isBlank()) {
            LAST_ERROR.set("Missing booking ID.");
            return false;
        }

        Booking target = getBookingById(bookingId);
        if (target == null) {
            LAST_ERROR.set("Booking not found.");
            return false;
        }
        if (target.getStatus() == null || !target.getStatus().equalsIgnoreCase("CONFIRMED")) {
            LAST_ERROR.set("Only CONFIRMED bookings can be rescheduled.");
            return false;
        }
        if (newCheckIn == null || newCheckOut == null || !newCheckOut.isAfter(newCheckIn)) {
            LAST_ERROR.set("Invalid dates. Check-out must be after check-in.");
            return false;
        }
        if (newCheckIn.isBefore(LocalDate.now())) {
            LAST_ERROR.set("Check-in date cannot be in the past.");
            return false;
        }

        Room room = getRoomById(target.getRoomId());
        if (room == null) {
            LAST_ERROR.set("Room not found.");
            return false;
        }
        if (!room.isAvailable()) {
            LAST_ERROR.set("This room is currently unavailable.");
            return false;
        }
        if (newGuests <= 0 || newGuests > room.getCapacity()) {
            LAST_ERROR.set("Guest count must be between 1 and " + room.getCapacity() + ".");
            return false;
        }

        if (!isRoomAvailableForDatesExcludingBooking(room.getId(), target.getBookingId(), newCheckIn, newCheckOut)) {
            LAST_ERROR.set("Room is already booked for the selected dates.");
            return false;
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(newCheckIn, newCheckOut);
        if (nights <= 0) {
            LAST_ERROR.set("Invalid stay length.");
            return false;
        }
        double total = room.getPricePerNight() * nights;

        target.setCheckInDate(newCheckIn);
        target.setCheckOutDate(newCheckOut);
        target.setNumberOfGuests(newGuests);
        target.setTotalPrice(total);
        TransactionFileHandler.updateBooking(target);
        TransactionFileHandler.updateLatestPaymentAmountForBooking(target.getBookingId(), total);

        return true;
    }
}
