package model;

import java.sql.Timestamp;

/**
 * BookingItem represents each individual seat reserved in a booking.
 */
public class BookingItem extends BaseEntity {
    private Long bookingId;
    private Long seatId;
    private double price;
    private String seatNumber; // convenience field for display

    public BookingItem() {
        super();
    }

    public BookingItem(Long id, Long bookingId, Long seatId, double price, String seatNumber, Timestamp createdAt) {
        super(id, createdAt);
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.price = price;
        this.seatNumber = seatNumber;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
}
