package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.AppException;
import model.User;
import util.SessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseHandler provides common utilities for all HTTP endpoints:
 * - CORS handling (including pre-flight OPTIONS)
 * - Request body reading
 * - Standardized JSON response formatting
 * - Centralized exception handling
 */
public abstract class BaseHandler implements HttpHandler {
    protected static final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set standard CORS headers for Next.js frontend
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            process(exchange);
        } catch (AppException e) {
            sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    protected abstract void process(HttpExchange exchange) throws Exception;

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = (data instanceof String) ? (String) data : gson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("error", true);
        err.put("status", statusCode);
        err.put("message", message);
        sendResponse(exchange, statusCode, err);
    }

    protected User getAuthenticatedUser(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "").trim();
        return SessionManager.getUser(token);
    }

    protected String getPathSegment(HttpExchange exchange, int index) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (index < parts.length) {
            return parts[index];
        }
        return null;
    }
}
