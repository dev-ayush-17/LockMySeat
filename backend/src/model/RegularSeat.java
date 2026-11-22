package model;

import java.sql.Timestamp;

/**
 * RegularSeat demonstrates Inheritance and Polymorphism.
 * Standard event seating priced at ₹250.
 */
public class RegularSeat extends Seat {
    public static final double PRICE = 250.0;

    public RegularSeat() {
        super();
        setSeatType("REGULAR");
    }

    public RegularSeat(Long id, Long eventId, String seatNumber, char rowLabel, int colNumber, String status, Timestamp createdAt) {
        super(id, eventId, seatNumber, rowLabel, colNumber, "REGULAR", status, createdAt);
    }

    @Override
    public double getPrice() {
        return PRICE;
    }
}
