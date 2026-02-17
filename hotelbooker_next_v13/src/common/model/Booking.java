package common.model;

import java.time.LocalDate;

public class Booking {
    private String bookingId;
    private String guestName;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private double totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED

    public Booking() {};

    public Booking(String bookingId, String guestName, String roomId, 
                   LocalDate checkInDate, LocalDate checkOutDate, 
                   int numberOfGuests, double totalPrice, String status) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public String getRoomId() { return roomId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    // Calculate number of nights
    public long getNumberOfNights() {
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    @Override
    public String toString() {
        return bookingId + "|" + guestName + "|" + roomId + "|" + 
               checkInDate + "|" + checkOutDate + "|" + numberOfGuests + "|" + 
               totalPrice + "|" + status;
    }
}
