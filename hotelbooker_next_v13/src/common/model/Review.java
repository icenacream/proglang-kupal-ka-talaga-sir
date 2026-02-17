package common.model;

import java.time.LocalDate;

public class Review {
    private final String roomId;
    private final String userEmail;
    private final int rating; // 1..5
    private final String comment;
    private final LocalDate date;

    public Review(String roomId, String userEmail, int rating, String comment, LocalDate date) {
        this.roomId = roomId;
        this.userEmail = userEmail;
        this.rating = rating;
        this.comment = comment == null ? "" : comment;
        this.date = date;
    }

    public String getRoomId() { return roomId; }
    public String getUserEmail() { return userEmail; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDate getDate() { return date; }
}
