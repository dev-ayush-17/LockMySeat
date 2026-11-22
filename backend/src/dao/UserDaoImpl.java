package dao;

import model.Role;
import model.User;
import util.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

/**
 * UserDaoImpl implements UserDao using plain JDBC PreparedStatement queries.
 */
public class UserDaoImpl implements UserDao {

    @Override
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, salt, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getRole().name());
            ps.setTimestamp(6, user.getCreatedAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        throw new SQLException("Failed to insert user.");
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, salt, role, created_at FROM users WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, salt, role, created_at FROM users WHERE LOWER(email) = LOWER(?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("salt"),
            Role.valueOf(rs.getString("role")),
            rs.getTimestamp("created_at")
        );
    }
}
