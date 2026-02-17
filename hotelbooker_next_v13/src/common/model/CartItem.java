package common.model;

import java.time.LocalDate;

public class CartItem {
    private final String roomId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guests;

    public CartItem(String roomId, LocalDate checkIn, LocalDate checkOut, int guests) {
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
    }

    public String getRoomId() { return roomId; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuests() { return guests; }
}
