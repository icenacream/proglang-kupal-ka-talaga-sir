package admin.service;

import common.filehandler.TransactionFileHandler;
import common.model.Booking;
import common.model.Payment;
import common.model.Room;
import common.model.User;
import common.service.UserService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Lightweight analytics computed from the local txt files.
 */
public class AdminAnalyticsService {

    public double getTotalRevenue() {
        double sum = 0;
        for (Payment p : TransactionFileHandler.readPaymentsFromFile()) {
            if (p != null && p.getStatus() != null && p.getStatus().equalsIgnoreCase("PAID")) {
                sum += p.getAmount();
            }
        }
        return sum;
    }

    public int getTotalBookings() {
        return TransactionFileHandler.readBookingsFromFile().size();
    }

    public int getActiveGuests() {
        List<User> users = UserService.getInstance().getAllUsers();
        return users.size();
    }

    public int getHotelsCount() {
        Set<String> hotels = new HashSet<>();
        for (Room r : TransactionFileHandler.readRoomsFromFile()) {
            if (r != null && r.getHotelName() != null) hotels.add(r.getHotelName().trim());
        }
        return hotels.size();
    }

    public Map<String, Integer> getBookingsByStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("CONFIRMED", 0);
        map.put("CANCELLED", 0);
        for (Booking b : TransactionFileHandler.readBookingsFromFile()) {
            String s = b.getStatus() == null ? "" : b.getStatus().toUpperCase(Locale.ROOT);
            if (map.containsKey(s)) map.put(s, map.get(s) + 1);
            else map.put(s, map.getOrDefault(s, 0) + 1);
        }
        return map;
    }

    /**
     * Revenue by month for last N months (including current month).
     */
    public Map<String, Double> getRevenueByMonth(int monthsBack) {
        Map<YearMonth, Double> tmp = new TreeMap<>();
        YearMonth now = YearMonth.now();
        for (int i = monthsBack - 1; i >= 0; i--) {
            tmp.put(now.minusMonths(i), 0.0);
        }

        for (Payment p : TransactionFileHandler.readPaymentsFromFile()) {
            if (p == null || p.getPaymentDate() == null) continue;
            if (p.getStatus() == null || !p.getStatus().equalsIgnoreCase("PAID")) continue;
            YearMonth ym = YearMonth.from(p.getPaymentDate());
            if (tmp.containsKey(ym)) {
                tmp.put(ym, tmp.get(ym) + p.getAmount());
            }
        }

        Map<String, Double> out = new LinkedHashMap<>();
        for (Map.Entry<YearMonth, Double> e : tmp.entrySet()) {
            out.put(e.getKey().toString(), e.getValue());
        }
        return out;
    }

    public double getOccupancyRate() {
        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        if (rooms.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        Set<String> occupiedRoomIds = new HashSet<>();
        for (common.model.Booking b : TransactionFileHandler.readBookingsFromFile()) {
            if (b == null) continue;
            if (b.getStatus() == null || !b.getStatus().equalsIgnoreCase("CONFIRMED")) continue;
            if (!today.isBefore(b.getCheckInDate()) && today.isBefore(b.getCheckOutDate())) {
                occupiedRoomIds.add(b.getRoomId());
            }
        }
        return occupiedRoomIds.size() / (double) rooms.size();
    }

    public Map<String, Integer> getOccupancyByHotel() {
        List<Room> rooms = TransactionFileHandler.readRoomsFromFile();
        Map<String, Integer> out = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        Set<String> occupiedRoomIds = new HashSet<>();
        for (common.model.Booking b : TransactionFileHandler.readBookingsFromFile()) {
            if (b == null) continue;
            if (b.getStatus() == null || !b.getStatus().equalsIgnoreCase("CONFIRMED")) continue;
            if (!today.isBefore(b.getCheckInDate()) && today.isBefore(b.getCheckOutDate())) {
                occupiedRoomIds.add(b.getRoomId());
            }
        }

        for (Room r : rooms) {
            if (r == null) continue;
            String key = r.getHotelName();
            out.putIfAbsent(key, 0);
            if (occupiedRoomIds.contains(r.getId())) out.put(key, out.get(key) + 1);
        }
        return out;
    }
}
