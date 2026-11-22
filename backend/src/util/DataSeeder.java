package util;

import dao.EventDao;
import dao.EventDaoImpl;
import dao.SeatDao;
import dao.SeatDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;
import model.Event;
import model.RegularSeat;
import model.Role;
import model.Seat;
import model.User;
import model.VipSeat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DataSeeder populates initial test data required by the specification:
 * - 1 Admin User (admin@lockmyseat.com / Admin@123)
 * - 1 Normal User (john@example.com / User@123)
 * - 6 Realistic Events across major cities (Mumbai, Bengaluru, Delhi, etc.)
 * - Auto-generates 30 seats per event (Rows A-B VIP @ ₹500, Rows C-E Regular @ ₹250)
 */
public class DataSeeder {
    private final UserDao userDao = new UserDaoImpl();
    private final EventDao eventDao = new EventDaoImpl();
    private final SeatDao seatDao = new SeatDaoImpl();

    public void seed() {
        try {
            seedUsers();
            seedEventsAndSeats();
            System.out.println("Data seeding completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during data seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedUsers() throws Exception {
        // 1. Admin user
        if (userDao.findByEmail("admin@lockmyseat.com").isEmpty()) {
            String adminSalt = PasswordUtil.generateSalt();
            String adminHash = PasswordUtil.hashPassword("Admin@123", adminSalt);
            User admin = new User(null, "System Administrator", "admin@lockmyseat.com", 
                                  adminHash, adminSalt, Role.ADMIN, new Timestamp(System.currentTimeMillis()));
            userDao.create(admin);
            System.out.println("Seeded admin user: admin@lockmyseat.com / Admin@123");
        }

        // 2. Normal user
        if (userDao.findByEmail("john@example.com").isEmpty()) {
            String userSalt = PasswordUtil.generateSalt();
            String userHash = PasswordUtil.hashPassword("User@123", userSalt);
            User user = new User(null, "John Doe", "john@example.com", 
                                 userHash, userSalt, Role.USER, new Timestamp(System.currentTimeMillis()));
            userDao.create(user);
            System.out.println("Seeded normal user: john@example.com / User@123");
        }
    }

    private void seedEventsAndSeats() throws Exception {
        List<Event> existing = eventDao.findAll();
        if (!existing.isEmpty()) {
            System.out.println("Events already seeded (" + existing.size() + " events found). Skipping event seeding.");
            return;
        }

        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        Event[] eventsToSeed = new Event[] {
            new Event(null, "A.R. Rahman Live in Concert", "Experience the musical maestro live with full orchestral ensemble.", 
                      "Mumbai", "DY Patil Stadium", new Timestamp(now + 7 * day), 30, 30, new Timestamp(now)),
            new Event(null, "Sunburn Arena EDM Night", "Electrifying EDM beats with world-renowned DJs.", 
                      "Bengaluru", "Manpho Convention Grounds", new Timestamp(now + 12 * day), 30, 30, new Timestamp(now)),
            new Event(null, "Zakir Khan - Tathastu Standup", "An evening of laughter, relatable stories, and signature comedy.", 
                      "Delhi", "Siri Fort Auditorium", new Timestamp(now + 15 * day), 30, 30, new Timestamp(now)),
            new Event(null, "International Tech & AI Summit 2026", "Keynotes on generative AI, cloud computing, and next-gen engineering.", 
                      "Hyderabad", "HICC Convention Centre", new Timestamp(now + 20 * day), 30, 30, new Timestamp(now)),
            new Event(null, "IPL Fan Park & Screening", "Cheer for your favorite team with giant LED screens, food stalls and music.", 
                      "Chennai", "Express Avenue Courtyard", new Timestamp(now + 25 * day), 30, 30, new Timestamp(now)),
            new Event(null, "Kolkata Classical Music Festival", "An authentic celebration of classical ragas and instrumental melodies.", 
                      "Kolkata", "Rabindra Sadan", new Timestamp(now + 30 * day), 30, 30, new Timestamp(now))
        };

        for (Event event : eventsToSeed) {
            Event created = eventDao.create(event);
            generateSeatsForEvent(created.getId());
            System.out.println("Seeded event: " + created.getTitle() + " in " + created.getCity() + " with 30 seats.");
        }
    }

    public void generateSeatsForEvent(Long eventId) throws Exception {
        char[] rows = new char[]{'A', 'B', 'C', 'D', 'E'};
        List<Seat> seats = new ArrayList<>();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        for (char row : rows) {
            for (int col = 1; col <= 6; col++) {
                String seatNo = "" + row + col;
                // Rows A and B are VIP (₹500); Rows C, D, E are Regular (₹250)
                if (row == 'A' || row == 'B') {
                    seats.add(new VipSeat(null, eventId, seatNo, row, col, "AVAILABLE", now));
                } else {
                    seats.add(new RegularSeat(null, eventId, seatNo, row, col, "AVAILABLE", now));
                }
            }
        }
        seatDao.createBatch(seats);
    }

    public static void main(String[] args) {
        new DataSeeder().seed();
    }
}
