package service;

import dao.EventDao;
import dao.EventDaoImpl;
import dao.SeatDao;
import dao.SeatDaoImpl;
import exception.AppException;
import exception.ResourceNotFoundException;
import model.Event;
import model.RegularSeat;
import model.Seat;
import model.VipSeat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * EventServiceImpl coordinates event creation and automatic seat generation.
 */
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;
    private final SeatDao seatDao;

    public EventServiceImpl() {
        this.eventDao = new EventDaoImpl();
        this.seatDao = new SeatDaoImpl();
    }

    public EventServiceImpl(EventDao eventDao, SeatDao seatDao) {
        this.eventDao = eventDao;
        this.seatDao = seatDao;
    }

    @Override
    public List<Event> getAllEvents() throws Exception {
        return eventDao.findAll();
    }

    @Override
    public List<Event> searchEvents(String query, String city) throws Exception {
        return eventDao.findBySearch(query, city);
    }

    @Override
    public Event getEventById(Long id) throws AppException, Exception {
        Optional<Event> eventOpt = eventDao.findById(id);
        if (eventOpt.isEmpty()) {
            throw new ResourceNotFoundException("Event with ID " + id + " not found");
        }
        return eventOpt.get();
    }

    @Override
    public Event createEvent(String title, String description, String city, String venue, String dateStr) throws AppException, Exception {
        if (title == null || title.trim().isEmpty()) {
            throw new AppException("Title is required", 400);
        }
        if (city == null || city.trim().isEmpty()) {
            throw new AppException("City is required", 400);
        }
        if (venue == null || venue.trim().isEmpty()) {
            throw new AppException("Venue is required", 400);
        }

        Timestamp eventDate;
        try {
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                eventDate = Timestamp.from(Instant.parse(dateStr.trim()));
            } else {
                eventDate = new Timestamp(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000);
            }
        } catch (DateTimeParseException e) {
            throw new AppException("Invalid ISO date format (use YYYY-MM-DDTHH:MM:SSZ)", 400);
        }

        Event event = new Event(null, title.trim(), description != null ? description.trim() : "", 
                                city.trim(), venue.trim(), eventDate, 30, 30, new Timestamp(System.currentTimeMillis()));
        Event created = eventDao.create(event);

        // Auto-generate 30 seats: Rows A-B VIP (₹500), Rows C-E Regular (₹250)
        char[] rows = new char[]{'A', 'B', 'C', 'D', 'E'};
        List<Seat> seats = new ArrayList<>();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        for (char row : rows) {
            for (int col = 1; col <= 6; col++) {
                String seatNo = "" + row + col;
                if (row == 'A' || row == 'B') {
                    seats.add(new VipSeat(null, created.getId(), seatNo, row, col, "AVAILABLE", now));
                } else {
                    seats.add(new RegularSeat(null, created.getId(), seatNo, row, col, "AVAILABLE", now));
                }
            }
        }
        seatDao.createBatch(seats);

        return created;
    }

    @Override
    public boolean deleteEvent(Long id) throws AppException, Exception {
        if (eventDao.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Event with ID " + id + " not found");
        }
        return eventDao.delete(id);
    }
}
