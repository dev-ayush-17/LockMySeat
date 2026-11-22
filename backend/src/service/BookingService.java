package service;

import exception.AppException;
import model.Booking;

import java.util.List;

/**
 * BookingService demonstrates Abstraction.
 */
public interface BookingService {
    Booking createBooking(Long userId, Long eventId, List<Long> seatIds) throws AppException, Exception;
    List<Booking> getUserBookings(Long userId) throws Exception;
    boolean cancelBooking(Long userId, Long bookingId) throws AppException, Exception;
}
