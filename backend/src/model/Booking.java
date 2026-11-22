package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking entity representing an order placed by a User for an Event.
 */
public class Booking extends BaseEntity {
    private Long userId;
    private Long eventId;
    private double totalAmount;
    private String status; // "CONFIRMED", "CANCELLED"
    private Timestamp bookingDate;
    private List<BookingItem> items;

    // Transient display fields for frontend convenience
    private String eventTitle;
    private String eventCity;
    private Timestamp eventDate;

    public Booking() {
        super();
        this.status = "CONFIRMED";
        this.bookingDate = new Timestamp(System.currentTimeMillis());
        this.items = new ArrayList<>();
    }

    public Booking(Long id, Long userId, Long eventId, double totalAmount, 
                   String status, Timestamp bookingDate, Timestamp createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
        this.status = status != null ? status : "CONFIRMED";
        this.bookingDate = bookingDate != null ? bookingDate : new Timestamp(System.currentTimeMillis());
        this.items = new ArrayList<>();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public List<BookingItem> getItems() { return items; }
    public void setItems(List<BookingItem> items) { this.items = items; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getEventCity() { return eventCity; }
    public void setEventCity(String eventCity) { this.eventCity = eventCity; }

    public Timestamp getEventDate() { return eventDate; }
    public void setEventDate(Timestamp eventDate) { this.eventDate = eventDate; }
}
