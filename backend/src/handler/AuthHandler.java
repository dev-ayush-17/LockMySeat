package handler;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exception.AppException;
import exception.UnauthorizedException;
import model.User;
import service.AuthService;
import service.AuthServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthHandler processes endpoints:
 * - POST /api/auth/register
 * - POST /api/auth/login
 * - POST /api/auth/logout
 * - GET  /api/auth/me
 */
public class AuthHandler extends BaseHandler {
    private final AuthService authService;

    public AuthHandler() {
        this.authService = new AuthServiceImpl();
    }

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.endsWith("/register") && "POST".equalsIgnoreCase(method)) {
            handleRegister(exchange);
        } else if (path.endsWith("/login") && "POST".equalsIgnoreCase(method)) {
            handleLogin(exchange);
        } else if (path.endsWith("/logout") && "POST".equalsIgnoreCase(method)) {
            handleLogout(exchange);
        } else if (path.endsWith("/me") && "GET".equalsIgnoreCase(method)) {
            handleMe(exchange);
        } else {
            sendError(exchange, 404, "Endpoint not found: " + method + " " + path);
        }
    }

    private void handleRegister(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        String name = json.has("name") ? json.get("name").getAsString() : null;
        String email = json.has("email") ? json.get("email").getAsString() : null;
        String password = json.has("password") ? json.get("password").getAsString() : null;

        User user = authService.register(name, email, password);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "User registered successfully");
        resp.put("userId", user.getId());
        resp.put("email", user.getEmail());
        resp.put("name", user.getName());
        sendResponse(exchange, 201, resp);
    }

    private void handleLogin(HttpExchange exchange) throws Exception {
        String body = readRequestBody(exchange);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        String email = json.has("email") ? json.get("email").getAsString() : null;
        String password = json.has("password") ? json.get("password").getAsString() : null;

        Map<String, Object> loginData = authService.login(email, password);
        sendResponse(exchange, 200, loginData);
    }

    private void handleLogout(HttpExchange exchange) throws Exception {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null) {
            String token = authHeader.replace("Bearer ", "").trim();
            authService.logout(token);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Logged out successfully");
        sendResponse(exchange, 200, resp);
    }

    private void handleMe(HttpExchange exchange) throws Exception {
        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            throw new UnauthorizedException("Not logged in");
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", user.getId());
        resp.put("name", user.getName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole().name());
        sendResponse(exchange, 200, resp);
    }
}
