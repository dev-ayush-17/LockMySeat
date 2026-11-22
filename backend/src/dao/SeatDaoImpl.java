package dao;

import model.RegularSeat;
import model.Seat;
import model.VipSeat;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SeatDaoImpl provides database operations for seats.
 * Instantiates concrete subclasses (RegularSeat, VipSeat) using Polymorphism.
 */
public class SeatDaoImpl implements SeatDao {

    @Override
    public void createBatch(List<Seat> seats) throws SQLException {
        String sql = "INSERT INTO seats (event_id, seat_number, row_label, col_number, seat_type, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Seat seat : seats) {
                ps.setLong(1, seat.getEventId());
                ps.setString(2, seat.getSeatNumber());
                ps.setString(3, String.valueOf(seat.getRowLabel()));
                ps.setInt(4, seat.getColNumber());
                ps.setString(5, seat.getSeatType());
                ps.setString(6, seat.getStatus());
                ps.setTimestamp(7, seat.getCreatedAt());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<Seat> findByEventId(Long eventId) throws SQLException {
        String sql = "SELECT id, event_id, seat_number, row_label, col_number, seat_type, status, created_at " +
                     "FROM seats WHERE event_id = ? ORDER BY row_label ASC, col_number ASC;";
        List<Seat> seats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRowToSeat(rs));
                }
            }
        }
        return seats;
    }

    @Override
    public Optional<Seat> findById(Long id) throws SQLException {
        String sql = "SELECT id, event_id, seat_number, row_label, col_number, seat_type, status, created_at " +
                     "FROM seats WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSeat(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * ATOMIC CONDITIONAL UPDATE:
     * This is the core Database-level double-booking defense!
     * Returns true ONLY if the seat was currently 'AVAILABLE' and was updated to 'BOOKED'.
     */
    @Override
    public boolean lockAndBookSeat(Connection conn, Long seatId) throws SQLException {
        String sql = "UPDATE seats SET status = 'BOOKED' WHERE id = ? AND status = 'AVAILABLE';";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        }
    }

    @Override
    public boolean releaseSeat(Connection conn, Long seatId) throws SQLException {
        String sql = "UPDATE seats SET status = 'AVAILABLE' WHERE id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Demonstrates Polymorphism and Factory Pattern:
     * Instantiates VipSeat or RegularSeat based on the database column.
     */
    private Seat mapRowToSeat(ResultSet rs) throws SQLException {
        String type = rs.getString("seat_type");
        Long id = rs.getLong("id");
        Long eventId = rs.getLong("event_id");
        String seatNumber = rs.getString("seat_number");
        char rowLabel = rs.getString("row_label").charAt(0);
        int colNumber = rs.getInt("col_number");
        String status = rs.getString("status");
        Timestamp createdAt = rs.getTimestamp("created_at");

        if ("VIP".equalsIgnoreCase(type)) {
            return new VipSeat(id, eventId, seatNumber, rowLabel, colNumber, status, createdAt);
        } else {
            return new RegularSeat(id, eventId, seatNumber, rowLabel, colNumber, status, createdAt);
        }
    }
}
