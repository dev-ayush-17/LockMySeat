package handler;

import com.sun.net.httpserver.HttpExchange;
import model.Seat;
import service.SeatService;
import service.SeatServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SeatHandler processes endpoint:
 * - GET /api/events/{id}/seats
 * Formats seats with explicit price for polymorphic VIP vs Regular calculation.
 */
public class SeatHandler extends BaseHandler {
    private final SeatService seatService;

    public SeatHandler() {
        this.seatService = new SeatServiceImpl();
    }

    public SeatHandler(SeatService seatService) {
        this.seatService = seatService;
    }

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Match /api/events/{id}/seats
        if (path.matches("^/api/events/\\d+/seats/?$") && "GET".equalsIgnoreCase(method)) {
            String[] parts = path.split("/");
            Long eventId = Long.parseLong(parts[3]);
            List<Seat> seats = seatService.getSeatsForEvent(eventId);

            // Enrich list with calculated polymorphic price for JSON serialization
            List<Map<String, Object>> seatDtos = new ArrayList<>();
            for (Seat seat : seats) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", seat.getId());
                map.put("eventId", seat.getEventId());
                map.put("seatNumber", seat.getSeatNumber());
                map.put("rowLabel", String.valueOf(seat.getRowLabel()));
                map.put("colNumber", seat.getColNumber());
                map.put("seatType", seat.getSeatType());
                map.put("status", seat.getStatus());
                map.put("price", seat.getPrice()); // Polymorphic call: VipSeat ₹500, RegularSeat ₹250
                seatDtos.add(map);
            }

            sendResponse(exchange, 200, seatDtos);
            return;
        }

        sendError(exchange, 404, "Endpoint not found: " + method + " " + path);
    }
}
