package service;

import dao.EventDao;
import dao.EventDaoImpl;
import dao.SeatDao;
import dao.SeatDaoImpl;
import exception.AppException;
import exception.ResourceNotFoundException;
import model.Seat;

import java.util.List;

public class SeatServiceImpl implements SeatService {
    private final EventDao eventDao;
    private final SeatDao seatDao;

    public SeatServiceImpl() {
        this.eventDao = new EventDaoImpl();
        this.seatDao = new SeatDaoImpl();
    }

    public SeatServiceImpl(EventDao eventDao, SeatDao seatDao) {
        this.eventDao = eventDao;
        this.seatDao = seatDao;
    }

    @Override
    public List<Seat> getSeatsForEvent(Long eventId) throws AppException, Exception {
        if (eventDao.findById(eventId).isEmpty()) {
            throw new ResourceNotFoundException("Event with ID " + eventId + " not found");
        }
        return seatDao.findByEventId(eventId);
    }
}
