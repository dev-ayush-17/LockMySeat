package model;

import java.sql.Timestamp;

/**
 * VipSeat demonstrates Inheritance and Polymorphism.
 * Premium event seating priced at ₹500.
 */
public class VipSeat extends Seat {
    public static final double PRICE = 500.0;

    public VipSeat() {
        super();
        setSeatType("VIP");
    }

    public VipSeat(Long id, Long eventId, String seatNumber, char rowLabel, int colNumber, String status, Timestamp createdAt) {
        super(id, eventId, seatNumber, rowLabel, colNumber, "VIP", status, createdAt);
    }

    @Override
    public double getPrice() {
        return PRICE;
    }
}
