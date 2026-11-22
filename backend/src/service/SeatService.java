package service;

import exception.AppException;
import model.Seat;

import java.util.List;

/**
 * SeatService demonstrates Abstraction.
 */
public interface SeatService {
    List<Seat> getSeatsForEvent(Long eventId) throws AppException, Exception;
}
