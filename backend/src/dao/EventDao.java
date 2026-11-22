package dao;

import model.Event;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * EventDao demonstrates Abstraction and method Overloading polymorphism.
 */
public interface EventDao {
    Event create(Event event) throws SQLException;
    Optional<Event> findById(Long id) throws SQLException;
    List<Event> findAll() throws SQLException;
    List<Event> findBySearch(String query, String city) throws SQLException;
    boolean delete(Long id) throws SQLException;
}
