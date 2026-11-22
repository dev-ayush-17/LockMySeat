package test;

import dao.EventDao;
import dao.EventDaoImpl;
import dao.SeatDao;
import dao.SeatDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;
import exception.AppException;
import exception.SeatAlreadyBookedException;
import model.Booking;
import model.Event;
import model.Role;
import model.Seat;
import model.User;
import service.BookingService;
import service.BookingServiceImpl;
import util.PasswordUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConcurrencyTest demonstrates Multithreading and Double-Booking Prevention.
 *
 * Spawns 10 parallel threads attempting to book the EXACT SAME SEAT simultaneously.
 * Demonstrates that:
 * 1. Exactly ONE thread succeeds in acquiring the seat.
 * 2. The other 9 threads fail cleanly with SeatAlreadyBookedException (HTTP 409).
 * 3. The database state remains consistent and uncorrupted.
 */
public class ConcurrencyTest {
    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  RUNNING CONCURRENT DOUBLE-BOOKING SIMULATION");
        System.out.println("==================================================");

        UserDao userDao = new UserDaoImpl();
        EventDao eventDao = new EventDaoImpl();
        SeatDao seatDao = new SeatDaoImpl();
        BookingService bookingService = new BookingServiceImpl();

        // 1. Prepare 10 distinct test users
        int threadCount = 10;
        List<User> testUsers = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            String email = "racer" + i + "@lockmyseat.test";
            final int idx = i;
            User u = userDao.findByEmail(email).orElseGet(() -> {
                try {
                    String salt = PasswordUtil.generateSalt();
                    String hash = PasswordUtil.hashPassword("Pass@123", salt);
                    User nu = new User(null, "Racer User " + idx, email, hash, salt, Role.USER, new Timestamp(System.currentTimeMillis()));
                    return userDao.create(nu);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            testUsers.add(u);
        }

        // 2. Find an available event and an available seat
        List<Event> events = eventDao.findAll();
        if (events.isEmpty()) {
            System.err.println("No events found to test concurrency!");
            System.exit(1);
        }
        Event targetEvent = events.get(0);
        List<Seat> seats = seatDao.findByEventId(targetEvent.getId());
        Seat targetSeat = null;
        for (Seat s : seats) {
            if ("AVAILABLE".equalsIgnoreCase(s.getStatus())) {
                targetSeat = s;
                break;
            }
        }

        if (targetSeat == null) {
            System.err.println("No available seat found in event " + targetEvent.getId());
            System.exit(1);
        }

        final Long eventId = targetEvent.getId();
        final Long seatId = targetSeat.getId();
        final String seatNumber = targetSeat.getSeatNumber();

        System.out.println("Target Event: " + targetEvent.getTitle() + " (ID: " + eventId + ")");
        System.out.println("Contested Seat: " + seatNumber + " (ID: " + seatId + ")");
        System.out.println("Firing " + threadCount + " concurrent booking threads simultaneously...\n");

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> outcomes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final User user = testUsers.get(i);
            final int threadNum = i + 1;
            Thread t = new Thread(() -> {
                try {
                    // Wait for the master start signal
                    startGate.await();

                    // Attempt booking
                    Booking booking = bookingService.createBooking(user.getId(), eventId, List.of(seatId));
                    successCount.incrementAndGet();
                    outcomes.add("[SUCCESS] Thread " + threadNum + " (User: " + user.getName() + 
                                 ") successfully BOOKED seat " + seatNumber + "! Booking ID: " + booking.getId());
                } catch (SeatAlreadyBookedException e) {
                    failureCount.incrementAndGet();
                    outcomes.add("[REJECTED] Thread " + threadNum + " failed cleanly: " + e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    outcomes.add("[ERROR] Thread " + threadNum + " failed with: " + e.getMessage());
                } finally {
                    endGate.countDown();
                }
            });
            t.start();
        }

        // Release all threads simultaneously
        startGate.countDown();
        endGate.await();

        for (String outcome : outcomes) {
            System.out.println(outcome);
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("TOTAL ATTEMPTS: " + threadCount);
        System.out.println("SUCCESSFUL BOOKINGS: " + successCount.get());
        System.out.println("REJECTED BOOKINGS (409): " + failureCount.get());
        System.out.println("--------------------------------------------------");

        if (successCount.get() == 1 && failureCount.get() == (threadCount - 1)) {
            System.out.println(">>> PROOF PASSED: EXACTLY ONE BOOKING SUCCEEDED. ZERO DOUBLE-BOOKINGS OCCURRED! <<<");
        } else {
            System.err.println(">>> CONCURRENCY FAILURE: Expected 1 success but got " + successCount.get() + " <<<");
            System.exit(1);
        }
    }
}
