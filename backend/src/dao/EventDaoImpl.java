package dao;

import model.Event;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * EventDaoImpl provides hand-crafted PreparedStatement operations for Events.
 * Calculates availableSeats dynamically using subqueries for accuracy.
 */
public class EventDaoImpl implements EventDao {

    @Override
    public Event create(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, description, city, venue, event_date, total_seats, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCity());
            ps.setString(4, event.getVenue());
            ps.setTimestamp(5, event.getEventDate());
            ps.setInt(6, event.getTotalSeats());
            ps.setTimestamp(7, event.getCreatedAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    event.setId(rs.getLong("id"));
                    event.setCreatedAt(rs.getTimestamp("created_at"));
                    event.setAvailableSeats(event.getTotalSeats());
                    return event;
                }
            }
        }
        throw new SQLException("Failed to create event.");
    }

    @Override
    public Optional<Event> findById(Long id) throws SQLException {
        String sql = "SELECT e.id, e.title, e.description, e.city, e.venue, e.event_date, e.total_seats, e.created_at, " +
                     "COUNT(s.id) FILTER (WHERE s.status = 'AVAILABLE') as available_seats " +
                     "FROM events e " +
                     "LEFT JOIN seats s ON e.id = s.event_id " +
                     "WHERE e.id = ? " +
                     "GROUP BY e.id;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEvent(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Event> findAll() throws SQLException {
        return findBySearch(null, null);
    }

    @Override
    public List<Event> findBySearch(String query, String city) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT e.id, e.title, e.description, e.city, e.venue, e.event_date, e.total_seats, e.created_at, " +
            "COUNT(s.id) FILTER (WHERE s.status = 'AVAILABLE') as available_seats " +
            "FROM events e " +
            "LEFT JOIN seats s ON e.id = s.event_id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(e.title) LIKE ? OR LOWER(e.description) LIKE ?) ");
            String wild = "%" + query.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
        }

        if (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("all")) {
            sql.append("AND LOWER(e.city) = ? ");
            params.add(city.trim().toLowerCase());
        }

        sql.append("GROUP BY e.id ORDER BY e.event_date ASC;");

        List<Event> events = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        }
        return events;
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        return new Event(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("city"),
            rs.getString("venue"),
            rs.getTimestamp("event_date"),
            rs.getInt("total_seats"),
            rs.getInt("available_seats"),
            rs.getTimestamp("created_at")
        );
    }
}
