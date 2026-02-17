package common.util;

import common.model.Booking;
import common.model.Room;
import common.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

/**
 * Generates an email-style text receipt (TXT) for a booking.
 * Saved under: receipts/<bookingId>_receipt.txt
 */
public class TextReceiptGenerator {
    private static final String RECEIPTS_DIR = "receipts";

    public static Path generateEmailStyleReceipt(Booking booking, Room room, User user) throws IOException {
        if (booking == null) throw new IllegalArgumentException("booking is null");
        Files.createDirectories(Paths.get(RECEIPTS_DIR));
        Path out = Paths.get(RECEIPTS_DIR, booking.getBookingId() + "_receipt.txt");
        String txt = buildEmailStyleReceiptText(booking, room, user);
        Files.write(out, txt.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return out;
    }

    public static String buildEmailStyleReceiptText(Booking booking, Room room, User user) {
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
        String guest = booking.getGuestName() != null ? booking.getGuestName() : (user != null ? user.getFullName() : "Guest");
        String email = user != null ? user.getEmail() : "(not provided)";
        String hotel = room != null ? room.getHotelName() : "(Unknown Hotel)";
        String location = room != null ? room.getLocation() : "";

        String subject = "Subject: Booking Receipt - " + booking.getBookingId();
        StringBuilder sb = new StringBuilder();
        sb.append(subject).append("\n");
        sb.append("To: ").append(email).append("\n");
        sb.append("\n");
        sb.append("Hi ").append(guest).append(",\n\n");
        sb.append("Thank you for booking with us! Here is your receipt.\n\n");

        sb.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        sb.append("Status: ").append(booking.getStatus()).append("\n");
        sb.append("Hotel: ").append(hotel).append("\n");
        if (!location.isBlank()) sb.append("Location: ").append(location).append("\n");
        sb.append("Room ID: ").append(booking.getRoomId()).append("\n");
        sb.append("Check-in: ").append(booking.getCheckInDate().format(df)).append("\n");
        sb.append("Check-out: ").append(booking.getCheckOutDate().format(df)).append("\n");
        sb.append("Guests: ").append(booking.getNumberOfGuests()).append("\n");
        sb.append("Total Paid: " + CurrencyUtil.format(booking.getTotalPrice()) + "\n");

        sb.append("\n");
        sb.append("If you have questions, reply to this message with your Booking ID.\n\n");
        sb.append("Regards,\n");
        sb.append("Hotel Booking System\n");
        return sb.toString();
    }
}
