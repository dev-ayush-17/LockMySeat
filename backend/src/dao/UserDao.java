package dao;

import model.User;
import java.sql.SQLException;
import java.util.Optional;

/**
 * UserDao demonstrates Abstraction.
 * Decouples database queries for User entities from the rest of the application.
 */
public interface UserDao {
    User create(User user) throws SQLException;
    Optional<User> findById(Long id) throws SQLException;
    Optional<User> findByEmail(String email) throws SQLException;
}
