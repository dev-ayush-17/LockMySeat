package dao;

import model.Seat;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * SeatDao demonstrates Abstraction.
 */
public interface SeatDao {
    void createBatch(List<Seat> seats) throws SQLException;
    List<Seat> findByEventId(Long eventId) throws SQLException;
    Optional<Seat> findById(Long id) throws SQLException;
    boolean lockAndBookSeat(Connection conn, Long seatId) throws SQLException;
    boolean releaseSeat(Connection conn, Long seatId) throws SQLException;
}
