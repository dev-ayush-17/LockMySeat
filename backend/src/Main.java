import com.sun.net.httpserver.HttpServer;
import handler.AuthHandler;
import handler.BookingHandler;
import handler.EventHandler;
import handler.SeatHandler;
import util.DataSeeder;
import util.DatabaseConnection;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application entrypoint.
 * Starts the JDK com.sun.net.httpserver.HttpServer without external framework dependencies.
 */
public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("==================================================");
            System.out.println("  Starting LockMySeat Plain-Java Backend Server");
            System.out.println("==================================================");

            // Ensure seed data is initialized
            new DataSeeder().seed();

            int port = DatabaseConnection.getServerPort();
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Register REST endpoint context handlers
            server.createContext("/api/auth", new AuthHandler());
            server.createContext("/api/events", new EventHandler());
            // Register SeatHandler on subpath or route dynamically
            server.createContext("/api/events/", (exchange) -> {
                String path = exchange.getRequestURI().getPath();
                if (path.contains("/seats")) {
                    new SeatHandler().handle(exchange);
                } else {
                    new EventHandler().handle(exchange);
                }
            });
            server.createContext("/api/bookings", new BookingHandler());

            // Thread pool for processing concurrent HTTP connections
            server.setExecutor(Executors.newFixedThreadPool(20));

            server.start();
            System.out.println("LockMySeat server listening on http://localhost:" + port);
            System.out.println("Ready to serve requests.");

        } catch (Exception e) {
            System.err.println("Failed to start LockMySeat server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
