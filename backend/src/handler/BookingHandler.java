package handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import exception.AppException;
import exception.UnauthorizedException;
import model.Booking;
import model.User;
import service.BookingService;
import service.BookingServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookingHandler processes endpoints:
 * - POST /api/bookings (Create booking)
 * - GET  /api/bookings/my (List authenticated user's bookings)
 * - POST /api/bookings/{id}/cancel (Cancel booking)
 */
public class BookingHandler extends BaseHandler {
    private final BookingService bookingService;

    public BookingHandler() {
        this.bookingService = new BookingServiceImpl();
    }

    public BookingHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        User user = getAuthenticatedUser(exchange);
        if (user == null) {
            throw new UnauthorizedException("Authentication token required.");
        }

        if (path.matches("^/api/bookings/?$") && "POST".equalsIgnoreCase(method)) {
            handleCreate(exchange, user);
            return;
        }

        if (path.matches("^/api/bookings/my/?$") && "GET".equalsIgnoreCase(method)) {
            handleMyBookings(exchange, user);
            return;
        }

        if (path.matches("^/api/bookings/\\d+/cancel/?$") && "POST".equalsIgnoreCase(method)) {
            String[] parts = path.split("/");
            Long bookingId = Long.parseLong(parts[3]);
            handleCancel(exchange, user, bookingId);
            return;
        }

        sendError(exchange, 404, "Endpoint not found: " + method + " " + path);
    }

    private void handleCreate(HttpExchange exchange, User user) throws Exception {
        String body = readRequestBody(exchange);
        JsonObject json = gson.fromJson(body, JsonObject.class);

        if (!json.has("eventId") || !json.has("seatIds")) {
            throw new AppException("eventId and seatIds array are required", 400);
        }

        Long eventId = json.get("eventId").getAsLong();
        JsonArray seatArray = json.getAsJsonArray("seatIds");
        List<Long> seatIds = new ArrayList<>();
        for (JsonElement el : seatArray) {
            seatIds.add(el.getAsLong());
        }

        Booking booking = bookingService.createBooking(user.getId(), eventId, seatIds);
        sendResponse(exchange, 201, booking);
    }

    private void handleMyBookings(HttpExchange exchange, User user) throws Exception {
        List<Booking> bookings = bookingService.getUserBookings(user.getId());
        sendResponse(exchange, 200, bookings);
    }

    private void handleCancel(HttpExchange exchange, User user, Long bookingId) throws Exception {
        boolean cancelled = bookingService.cancelBooking(user.getId(), bookingId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", cancelled);
        resp.put("message", "Booking cancelled and seats released successfully");
        sendResponse(exchange, 200, resp);
    }
}
