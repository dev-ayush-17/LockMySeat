package dao;

import model.Booking;
import model.BookingItem;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookingDaoImpl provides booking and booking items persistence.
 * Designed to participate in caller-managed JDBC transactions.
 */
public class BookingDaoImpl implements BookingDao {

    @Override
    public Booking create(Connection conn, Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, event_id, total_amount, status, booking_date, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, booking.getUserId());
            ps.setLong(2, booking.getEventId());
            ps.setDouble(3, booking.getTotalAmount());
            ps.setString(4, booking.getStatus());
            ps.setTimestamp(5, booking.getBookingDate());
            ps.setTimestamp(6, booking.getCreatedAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    booking.setId(rs.getLong("id"));
                    booking.setCreatedAt(rs.getTimestamp("created_at"));
                    return booking;
                }
            }
        }
        throw new SQLException("Failed to create booking.");
    }

    @Override
    public void createItems(Connection conn, List<BookingItem> items) throws SQLException {
        String sql = "INSERT INTO booking_items (booking_id, seat_id, price, created_at) " +
                     "VALUES (?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BookingItem item : items) {
                ps.setLong(1, item.getBookingId());
                ps.setLong(2, item.getSeatId());
                ps.setDouble(3, item.getPrice());
                ps.setTimestamp(4, item.getCreatedAt());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<Booking> findById(Long id) throws SQLException {
        String sql = "SELECT b.id, b.user_id, b.event_id, b.total_amount, b.status, b.booking_date, b.created_at, " +
                     "e.title as event_title, e.city as event_city, e.event_date " +
                     "FROM bookings b " +
                     "JOIN events e ON b.event_id = e.id " +
                     "WHERE b.id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking b = mapRowToBooking(rs);
                    b.setItems(findItemsByBookingId(b.getId()));
                    return Optional.of(b);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT b.id, b.user_id, b.event_id, b.total_amount, b.status, b.booking_date, b.created_at, " +
                     "e.title as event_title, e.city as event_city, e.event_date " +
                     "FROM bookings b " +
                     "JOIN events e ON b.event_id = e.id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.booking_date DESC;";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapRowToBooking(rs);
                    b.setItems(findItemsByBookingId(b.getId()));
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }

    @Override
    public List<BookingItem> findItemsByBookingId(Long bookingId) throws SQLException {
        String sql = "SELECT bi.id, bi.booking_id, bi.seat_id, bi.price, bi.created_at, s.seat_number " +
                     "FROM booking_items bi " +
                     "JOIN seats s ON bi.seat_id = s.id " +
                     "WHERE bi.booking_id = ?;";
        List<BookingItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingItem item = new BookingItem(
                        rs.getLong("id"),
                        rs.getLong("booking_id"),
                        rs.getLong("seat_id"),
                        rs.getDouble("price"),
                        rs.getString("seat_number"),
                        rs.getTimestamp("created_at")
                    );
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Override
    public boolean cancelBooking(Connection conn, Long bookingId) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ? AND status = 'CONFIRMED';";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            return ps.executeUpdate() == 1;
        }
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("event_id"),
            rs.getDouble("total_amount"),
            rs.getString("status"),
            rs.getTimestamp("booking_date"),
            rs.getTimestamp("created_at")
        );
        b.setEventTitle(rs.getString("event_title"));
        b.setEventCity(rs.getString("event_city"));
        b.setEventDate(rs.getTimestamp("event_date"));
        return b;
    }
}
