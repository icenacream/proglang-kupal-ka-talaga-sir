package common.util;

import common.model.Booking;
import common.model.Room;
import common.model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tiny PDF writer (no external libraries) for simple one-page receipts.
 * Saves to receipts/Booking_<id>.pdf
 */
public class PdfReceiptGenerator {

    public static Path generateReceipt(Booking booking, Room room, User user) throws IOException {
        Files.createDirectories(Paths.get("receipts"));
        String fileName = "Booking_" + booking.getBookingId() + ".pdf";
        Path out = Paths.get("receipts", fileName);

        List<String> lines = new ArrayList<>();
        lines.add("HotelBooker - Booking Receipt");
        lines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lines.add("");
        lines.add("Booking ID: " + booking.getBookingId());
        if (user != null) {
            lines.add("Guest: " + user.getFullName() + " (" + user.getEmail() + ")");
        } else {
            lines.add("Guest: " + booking.getGuestName());
        }
        if (room != null) {
            lines.add("Hotel: " + room.getHotelName());
            lines.add("Location: " + room.getLocation());
        }
        lines.add("Check-in: " + booking.getCheckInDate());
        lines.add("Check-out: " + booking.getCheckOutDate());
        lines.add("Guests: " + booking.getNumberOfGuests());
        lines.add("Nights: " + booking.getNumberOfNights());
        lines.add("Total: " + CurrencyUtil.format(booking.getTotalPrice()));
        lines.add("Status: " + booking.getStatus());

        writeSimplePdf(out, lines);
        return out;
    }

    private static void writeSimplePdf(Path out, List<String> lines) throws IOException {
        // Build a simple content stream using Helvetica.
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("72 760 Td\n");
        for (int i = 0; i < lines.size(); i++) {
            String s = escapePdf(lines.get(i));
            content.append("(").append(s).append(") Tj\n");
            if (i != lines.size() - 1) {
                content.append("0 -16 Td\n");
            }
        }
        content.append("ET\n");
        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Integer> xref = new ArrayList<>();
        write(baos, "%PDF-1.4\n");

        // 1) Catalog
        xref.add(baos.size());
        write(baos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // 2) Pages
        xref.add(baos.size());
        write(baos, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // 3) Page
        xref.add(baos.size());
        write(baos,
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                "/Resources << /Font << /F1 4 0 R >> >> " +
                "/Contents 5 0 R >>\n" +
                "endobj\n");

        // 4) Font
        xref.add(baos.size());
        write(baos, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        // 5) Contents
        xref.add(baos.size());
        write(baos, "5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
        baos.write(contentBytes);
        write(baos, "\nendstream\nendobj\n");

        int xrefStart = baos.size();
        write(baos, "xref\n");
        write(baos, "0 " + (xref.size() + 1) + "\n");
        write(baos, String.format("%010d 65535 f \n", 0));
        for (Integer off : xref) {
            write(baos, String.format("%010d 00000 n \n", off));
        }

        write(baos, "trailer\n<< /Size " + (xref.size() + 1) + " /Root 1 0 R >>\n");
        write(baos, "startxref\n" + xrefStart + "\n%%EOF\n");

        Files.write(out, baos.toByteArray());
    }

    private static String escapePdf(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private static void write(OutputStream os, String s) throws IOException {
        os.write(s.getBytes(StandardCharsets.US_ASCII));
    }
}
