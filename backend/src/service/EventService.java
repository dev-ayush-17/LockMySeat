package service;

import exception.AppException;
import model.Event;

import java.util.List;

/**
 * EventService demonstrates Abstraction.
 */
public interface EventService {
    List<Event> getAllEvents() throws Exception;
    List<Event> searchEvents(String query, String city) throws Exception;
    Event getEventById(Long id) throws AppException, Exception;
    Event createEvent(String title, String description, String city, String venue, String dateStr) throws AppException, Exception;
    boolean deleteEvent(Long id) throws AppException, Exception;
}
