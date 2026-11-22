package dao;

import model.Booking;
import model.BookingItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * BookingDao demonstrates Abstraction.
 */
public interface BookingDao {
    Booking create(Connection conn, Booking booking) throws SQLException;
    void createItems(Connection conn, List<BookingItem> items) throws SQLException;
    Optional<Booking> findById(Long id) throws SQLException;
    List<Booking> findByUserId(Long userId) throws SQLException;
    List<BookingItem> findItemsByBookingId(Long bookingId) throws SQLException;
    boolean cancelBooking(Connection conn, Long bookingId) throws SQLException;
}
