package service;

import dao.BookingDao;
import dao.BookingDaoImpl;
import dao.EventDao;
import dao.EventDaoImpl;
import dao.SeatDao;
import dao.SeatDaoImpl;
import exception.AppException;
import exception.ResourceNotFoundException;
import exception.SeatAlreadyBookedException;
import exception.UnauthorizedException;
import model.Booking;
import model.BookingItem;
import model.Seat;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookingServiceImpl implements the core Double-Booking Prevention architecture:
 *
 * 1. Java-Level Concurrency Control:
 *    The bookSeats method is synchronized on a static lock object to prevent
 *    race conditions in multithreaded runtime execution.
 *
 * 2. JDBC Transaction Management:
 *    setAutoCommit(false), execute atomic updates, commit() on success,
 *    and rollback() in the catch block.
 *
 * 3. Database-Level Concurrency Defense:
 *    seatDao.lockAndBookSeat executes:
 *    "UPDATE seats SET status = 'BOOKED' WHERE id = ? AND status = 'AVAILABLE'"
 *    If affectedRows != 1, throws SeatAlreadyBookedException immediately.
 */
public class BookingServiceImpl implements BookingService {
    private static final Object BOOKING_LOCK = new Object();

    private final BookingDao bookingDao;
    private final SeatDao seatDao;
    private final EventDao eventDao;

    public BookingServiceImpl() {
        this.bookingDao = new BookingDaoImpl();
        this.seatDao = new SeatDaoImpl();
        this.eventDao = new EventDaoImpl();
    }

    public BookingServiceImpl(BookingDao bookingDao, SeatDao seatDao, EventDao eventDao) {
        this.bookingDao = bookingDao;
        this.seatDao = seatDao;
        this.eventDao = eventDao;
    }

    @Override
    public Booking createBooking(Long userId, Long eventId, List<Long> seatIds) throws AppException, Exception {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new AppException("At least one seat must be selected", 400);
        }
        if (seatIds.size() > 4) {
            throw new AppException("Maximum 4 seats allowed per booking", 400);
        }

        if (eventDao.findById(eventId).isEmpty()) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }

        // Java Level Protection: Synchronized lock across threads
        synchronized (BOOKING_LOCK) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                // 1. Begin Transaction
                conn.setAutoCommit(false);

                double totalAmount = 0.0;
                List<Seat> verifiedSeats = new ArrayList<>();

                // Verify and conditionally lock every requested seat
                for (Long seatId : seatIds) {
                    Optional<Seat> seatOpt = seatDao.findById(seatId);
                    if (seatOpt.isEmpty() || !seatOpt.get().getEventId().equals(eventId)) {
                        throw new AppException("Seat with ID " + seatId + " does not belong to this event", 400);
                    }
                    Seat seat = seatOpt.get();

                    // 2. Database Level Atomic Check & Update
                    boolean locked = seatDao.lockAndBookSeat(conn, seatId);
                    if (!locked) {
                        // Another transaction already took this seat!
                        throw new SeatAlreadyBookedException("Seat " + seat.getSeatNumber() + " is already booked!");
                    }

                    totalAmount += seat.getPrice(); // Polymorphic getPrice(): VIP ₹500, Regular ₹250
                    verifiedSeats.add(seat);
                }

                // 3. Insert Booking Header
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Booking booking = new Booking(null, userId, eventId, totalAmount, "CONFIRMED", now, now);
                Booking createdBooking = bookingDao.create(conn, booking);

                // 4. Insert Booking Items
                List<BookingItem> items = new ArrayList<>();
                for (Seat seat : verifiedSeats) {
                    BookingItem item = new BookingItem(null, createdBooking.getId(), seat.getId(), 
                                                       seat.getPrice(), seat.getSeatNumber(), now);
                    items.add(item);
                }
                bookingDao.createItems(conn, items);
                createdBooking.setItems(items);

                // 5. Commit Transaction
                conn.commit();
                return createdBooking;

            } catch (Exception e) {
                // Rollback on any failure
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        System.err.println("Rollback failed: " + ex.getMessage());
                    }
                }
                throw e;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
            }
        }
    }

    @Override
    public List<Booking> getUserBookings(Long userId) throws Exception {
        return bookingDao.findByUserId(userId);
    }

    @Override
    public boolean cancelBooking(Long userId, Long bookingId) throws AppException, Exception {
        synchronized (BOOKING_LOCK) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                Optional<Booking> bookingOpt = bookingDao.findById(bookingId);
                if (bookingOpt.isEmpty()) {
                    throw new ResourceNotFoundException("Booking with ID " + bookingId + " not found");
                }

                Booking booking = bookingOpt.get();
                if (!booking.getUserId().equals(userId)) {
                    throw new UnauthorizedException("You are not authorized to cancel this booking");
                }

                if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                    throw new AppException("Booking is already cancelled", 400);
                }

                // Cancel the booking status
                boolean cancelled = bookingDao.cancelBooking(conn, bookingId);
                if (!cancelled) {
                    throw new AppException("Could not cancel booking", 400);
                }

                // Release seats back to 'AVAILABLE'
                List<BookingItem> items = bookingDao.findItemsByBookingId(bookingId);
                for (BookingItem item : items) {
                    seatDao.releaseSeat(conn, item.getSeatId());
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        System.err.println("Rollback on cancel failed: " + ex.getMessage());
                    }
                }
                throw e;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
            }
        }
    }
}
