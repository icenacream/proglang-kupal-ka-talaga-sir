package common.model;

import java.time.LocalDateTime;

public class Payment {
    private String paymentId;
    private String bookingId;
    private double amount;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, NET_BANKING
    private String status; // PENDING, SUCCESS, FAILED
    private LocalDateTime paymentDate;

    public Payment(String paymentId, String bookingId, double amount, 
                   String paymentMethod, String status, LocalDateTime paymentDate) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public LocalDateTime getPaymentDate() { return paymentDate; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setAmount(double amount) { this.amount = amount; }

    @Override
    public String toString() {
        return paymentId + "|" + bookingId + "|" + amount + "|" + 
               paymentMethod + "|" + status + "|" + paymentDate;
    }
}
