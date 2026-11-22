package service;

import exception.AppException;
import model.User;

import java.util.Map;

/**
 * AuthService demonstrates Abstraction.
 */
public interface AuthService {
    User register(String name, String email, String password) throws Exception;
    Map<String, Object> login(String email, String password) throws AppException, Exception;
    void logout(String token);
    User validateToken(String token) throws AppException;
}
