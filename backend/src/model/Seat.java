package model;

import java.sql.Timestamp;

/**
 * Seat demonstrates Abstraction and Polymorphism.
 * Abstract class defining core seat properties and an abstract getPrice()
 * method implemented by concrete seat subclasses (VipSeat, RegularSeat).
 */
public abstract class Seat extends BaseEntity {
    private Long eventId;
    private String seatNumber; // e.g. "A1", "C3"
    private char rowLabel;     // e.g. 'A', 'B'
    private int colNumber;     // e.g. 1, 2, 3
    private String seatType;   // "VIP" or "REGULAR"
    private String status;     // "AVAILABLE", "BOOKED"

    public Seat() {
        super();
        this.status = "AVAILABLE";
    }

    public Seat(Long id, Long eventId, String seatNumber, char rowLabel, 
                int colNumber, String seatType, String status, Timestamp createdAt) {
        super(id, createdAt);
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.rowLabel = rowLabel;
        this.colNumber = colNumber;
        this.seatType = seatType;
        this.status = status != null ? status : "AVAILABLE";
    }

    /**
     * Polymorphic method overridden by subclasses.
     * VIP seats return ₹500, Regular seats return ₹250.
     */
    public abstract double getPrice();

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public char getRowLabel() { return rowLabel; }
    public void setRowLabel(char rowLabel) { this.rowLabel = rowLabel; }

    public int getColNumber() { return colNumber; }
    public void setColNumber(int colNumber) { this.colNumber = colNumber; }

    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
