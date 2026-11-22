package service;

import dao.UserDao;
import dao.UserDaoImpl;
import exception.AppException;
import exception.InvalidLoginException;
import exception.UnauthorizedException;
import model.Role;
import model.User;
import util.PasswordUtil;
import util.SessionManager;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AuthServiceImpl implements authentication logic.
 * Encapsulates password hashing and token generation.
 */
public class AuthServiceImpl implements AuthService {
    private final UserDao userDao;

    public AuthServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(String name, String email, String password) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new AppException("Name is required", 400);
        }
        if (email == null || !email.contains("@")) {
            throw new AppException("A valid email is required", 400);
        }
        if (password == null || password.length() < 6) {
            throw new AppException("Password must be at least 6 characters", 400);
        }

        if (userDao.findByEmail(email).isPresent()) {
            throw new AppException("User with email '" + email + "' already exists", 400);
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        User user = new User(null, name.trim(), email.trim(), hash, salt, Role.USER, new Timestamp(System.currentTimeMillis()));
        return userDao.create(user);
    }

    @Override
    public Map<String, Object> login(String email, String password) throws AppException, Exception {
        if (email == null || password == null) {
            throw new InvalidLoginException("Email and password are required");
        }

        Optional<User> userOpt = userDao.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            throw new InvalidLoginException("Invalid email or password");
        }

        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            throw new InvalidLoginException("Invalid email or password");
        }

        String token = SessionManager.createSession(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        return response;
    }

    @Override
    public void logout(String token) {
        SessionManager.removeSession(token);
    }

    @Override
    public User validateToken(String token) throws AppException {
        User user = SessionManager.getUser(token);
        if (user == null) {
            throw new UnauthorizedException("Invalid or expired session. Please login again.");
        }
        return user;
    }
}
