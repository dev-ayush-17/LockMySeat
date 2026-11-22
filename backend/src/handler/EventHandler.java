package handler;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exception.AppException;
import exception.UnauthorizedException;
import model.Event;
import model.Role;
import model.User;
import service.EventService;
import service.EventServiceImpl;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventHandler processes endpoints:
 * - GET    /api/events (with optional ?query=...&city=...)
 * - GET    /api/events/{id}
 * - POST   /api/events (Admin role required)
 * - DELETE /api/events/{id} (Admin role required)
 */
public class EventHandler extends BaseHandler {
    private final EventService eventService;

    public EventHandler() {
        this.eventService = new EventServiceImpl();
    }

    public EventHandler(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Exact match for /api/events or /api/events/
        if (path.matches("^/api/events/?$")) {
            if ("GET".equalsIgnoreCase(method)) {
                handleList(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreate(exchange);
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
            return;
        }

        // Subpath match: /api/events/{id}
        if (path.matches("^/api/events/\\d+/?$")) {
            String[] parts = path.split("/");
            Long eventId = Long.parseLong(parts[3]);

            if ("GET".equalsIgnoreCase(method)) {
                handleGetOne(exchange, eventId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange, eventId);
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
            return;
        }

        sendError(exchange, 404, "Endpoint not found: " + method + " " + path);
    }

    private void handleList(HttpExchange exchange) throws Exception {
        URI uri = exchange.getRequestURI();
        String query = null;
        String city = null;

        if (uri.getQuery() != null) {
            String[] pairs = uri.getQuery().split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    if ("query".equalsIgnoreCase(key) || "search".equalsIgnoreCase(key)) {
                        query = val;
                    } else if ("city".equalsIgnoreCase(key)) {
                        city = val;
                    }
                }
            }
        }

        List<Event> events = eventService.searchEvents(query, city);
        sendResponse(exchange, 200, events);
    }

    private void handleGetOne(HttpExchange exchange, Long id) throws Exception {
        Event event = eventService.getEventById(id);
        sendResponse(exchange, 200, event);
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        User user = getAuthenticatedUser(exchange);
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Admin access required to create an event.");
        }

        String body = readRequestBody(exchange);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        String title = json.has("title") ? json.get("title").getAsString() : null;
        String desc = json.has("description") ? json.get("description").getAsString() : "";
        String city = json.has("city") ? json.get("city").getAsString() : null;
        String venue = json.has("venue") ? json.get("venue").getAsString() : null;
        String date = json.has("eventDate") ? json.get("eventDate").getAsString() : null;

        Event created = eventService.createEvent(title, desc, city, venue, date);
        sendResponse(exchange, 201, created);
    }

    private void handleDelete(HttpExchange exchange, Long id) throws Exception {
        User user = getAuthenticatedUser(exchange);
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Admin access required to delete an event.");
        }

        boolean deleted = eventService.deleteEvent(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", deleted);
        resp.put("message", "Event deleted successfully");
        sendResponse(exchange, 200, resp);
    }
}
