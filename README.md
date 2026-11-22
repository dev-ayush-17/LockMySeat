# 🎟️ LockMySeat — Live Event Ticketing & Seat Reservation System

A full-stack event ticketing prototype designed for a college **Object-Oriented Programming (OOPS) Lab**. It demonstrates clean OOP architecture, JDBC transactions, and multi-threaded double-booking prevention in **Plain Java 21 (Zero Frameworks)** coupled with a responsive **Next.js (App Router) + Tailwind CSS** frontend.

---

## 🚀 Live Demo & Access

- **Frontend Application:** [http://localhost:3000](http://localhost:3000)
- **Backend REST API:** [http://localhost:8081](http://localhost:8081)
- **PostgreSQL Database:** `localhost:5432/lockmyseat`

### 🔑 Pre-Seeded Test Credentials
| Role | Email | Password | Permissions |
|---|---|---|---|
| **Admin** | `admin@lockmyseat.com` | `Admin@123` | Create events (auto-generates 30 seats), delete events, book & cancel |
| **Normal User** | `john@example.com` | `User@123` | Browse events, view seat matrix, book 1-4 seats, cancel bookings |

---

## 🛠️ Tech Stack & Constraints

- **Backend:** Plain Java 21 (OpenJDK 21.0.10)
  - **No Spring / Spring Boot / Hibernate / JPA / Lombok.**
  - **HTTP Server:** Built-in JDK `com.sun.net.httpserver.HttpServer`.
  - **Database Access:** Plain JDBC with PostgreSQL Driver (`postgresql-42.7.3.jar`).
  - **JSON:** Google Gson (`gson-2.10.1.jar`).
  - **Security:** Pure Java `MessageDigest` SHA-256 with 16-byte cryptographically secure random salt; in-memory `ConcurrentHashMap` session tokens.
- **Database:** PostgreSQL 18.4
- **Frontend:** Next.js 16 (App Router, TypeScript) + Tailwind CSS

---

## 🎓 Viva & OOPS Concepts Mapping

This project was intentionally engineered without frameworks so that **every file and design decision can be explained in a viva using core Java concepts**:

| OOPS Concept | Code Implementation | Files to Reference in Viva |
|---|---|---|
| **Encapsulation** | Private instance variables with strict validation getters and setters. Sensitive hash/salt isolated. | `model/BaseEntity.java`<br>`model/User.java`<br>`model/Seat.java` |
| **Inheritance** | Single-table / polymorphic hierarchy where `RegularSeat` and `VipSeat` extend `abstract Seat`, and all domain models extend `abstract BaseEntity`. | `model/RegularSeat.java`<br>`model/VipSeat.java`<br>`model/BaseEntity.java` |
| **Polymorphism** | Concrete subclasses override `public abstract double getPrice()`. `VipSeat` returns ₹500, `RegularSeat` returns ₹250. Dynamic dispatch determines total booking cost. Method overloading in DAOs. | `model/Seat.java`<br>`model/VipSeat.java`<br>`model/RegularSeat.java` |
| **Abstraction** | Clear contract interfaces (`UserDao`, `EventDao`, `SeatDao`, `BookingDao`, `BookingService`, `AuthService`) decoupling data storage and business logic from HTTP presentation. | `dao/*.java`<br>`service/*.java` |
| **Custom Exceptions** | Domain-specific exception hierarchy extending `AppException` (`SeatAlreadyBookedException`, `InvalidLoginException`, `UnauthorizedException`) caught centrally and mapped to HTTP status codes (409, 401, 403, 404). | `exception/AppException.java`<br>`exception/SeatAlreadyBookedException.java`<br>`handler/BaseHandler.java` |
| **Collections & Generics** | Heavy natural usage of `List<Seat>`, `Map<String, Object>`, `Optional<T>`, and `ConcurrentHashMap<String, User>`. | `util/SessionManager.java`<br>`service/BookingServiceImpl.java` |
| **Multithreading & Concurrency** | Synchronized transaction execution in `BookingServiceImpl` paired with `test/ConcurrencyTest.java` (spawning 10 threads with `CountDownLatch`). | `service/BookingServiceImpl.java`<br>`test/ConcurrencyTest.java` |
| **File I/O** | Loading external runtime credentials from `config.properties` using `java.util.Properties` and `FileInputStream`. | `util/DatabaseConnection.java` |

---

## 🛡️ Double-Booking Prevention: How It Works

LockMySeat enforces double-booking prevention at **TWO independent layers**:

```text
[HTTP Request: Book Seat A1]
            │
            ▼
┌────────────────────────────────────────────────────────┐
│ 1. Application-Level Concurrency Control               │
│    - synchronized (BOOKING_LOCK) ensures only one      │
│      thread executes the critical booking block.       │
│    - Begins JDBC Transaction: setAutoCommit(false)     │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ 2. Database-Level Atomic Conditional Update            │
│    UPDATE seats                                        │
│    SET status = 'BOOKED'                               │
│    WHERE id = ? AND status = 'AVAILABLE';              │
└───────────────────────────┬────────────────────────────┘
                            │
              ┌─────────────┴─────────────┐
        affectedRows == 1           affectedRows == 0
              │                           │
              ▼                           ▼
┌───────────────────────────┐ ┌──────────────────────────┐
│ INSERT INTO bookings;     │ │ Rollback transaction;    │
│ INSERT INTO booking_items;│ │ Throw                    │
│ conn.commit();            │ │ SeatAlreadyBookedException│
│ HTTP 201 Created          │ │ (HTTP 409 Conflict)      │
└───────────────────────────┘ └──────────────────────────┘
```

### Multithreaded Verification Proof
You can run the concurrency test at any time:
```powershell
# From project root:
& "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -cp "backend\bin;backend\lib\postgresql-42.7.3.jar;backend\lib\gson-2.10.1.jar" test.ConcurrencyTest
```
**Actual Result:** 10 threads simultaneously contested seat `A1`:
- 1 thread succeeded and committed Booking #1.
- 9 threads received `SeatAlreadyBookedException` and rolled back.
- Exactly 1 ticket was issued.

---

## 📂 Project Structure

```text
LockMySeat/
├── README.md                          # Full viva documentation and runbook
├── backend/
│   ├── config.properties              # Local DB connection & port (git-ignored)
│   ├── config.properties.example      # Template for setup
│   ├── lib/                           # postgresql-42.7.3.jar, gson-2.10.1.jar
│   ├── bin/                           # Compiled .class files
│   ├── src/
│   │   ├── Main.java                  # JDK HttpServer router & thread pool startup
│   │   ├── schema.sql                 # PostgreSQL DDL
│   │   ├── model/                     # BaseEntity, User, Role, Event, Seat, VipSeat, RegularSeat, Booking, BookingItem
│   │   ├── dao/                       # UserDao, EventDao, SeatDao, BookingDao + Implementations
│   │   ├── service/                   # AuthService, EventService, SeatService, BookingService + Impls
│   │   ├── handler/                   # BaseHandler (CORS/JSON), AuthHandler, EventHandler, SeatHandler, BookingHandler
│   │   ├── exception/                 # AppException, SeatAlreadyBookedException, InvalidLoginException, etc.
│   │   └── util/                      # DatabaseConnection, PasswordUtil, SessionManager, DataSeeder
│   └── test/
│       ├── TestDbConnection.java      # JDBC verification test
│       └── ConcurrencyTest.java       # Multithreaded 10-thread collision test
└── frontend/                          # Next.js 16 App Router + Tailwind CSS
    ├── app/
    │   ├── page.tsx                   # Event browse, live search, city filter
    │   ├── login/page.tsx             # Login form with one-click test credentials
    │   ├── register/page.tsx          # Account registration
    │   ├── events/[id]/page.tsx       # Live seat matrix, pricing summation, booking
    │   ├── my-bookings/page.tsx       # Booking history & instant seat cancellation
    │   └── admin/page.tsx             # Admin: create event (auto-seats) & delete event
    ├── components/
    │   └── Navbar.tsx                 # Dynamic navbar with role detection
    └── lib/
        └── api.ts                     # Type-safe API client and session management
```

---

## ⚡ How to Run Everything

### 1. Database Setup
PostgreSQL must be running on `localhost:5432`. Ensure `backend/config.properties` has your credentials:
```properties
db.url=jdbc:postgresql://localhost:5432/lockmyseat
db.user=postgres
db.password=breaking@bad
server.port=8081
```

### 2. Run Backend
```powershell
cd backend
# Compile (if modified):
& "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" -cp "lib\postgresql-42.7.3.jar;lib\gson-2.10.1.jar;src" -d "bin" (Get-ChildItem -Path "src" -Recurse -Filter "*.java").FullName

# Start Server:
& "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -cp "bin;lib\postgresql-42.7.3.jar;lib\gson-2.10.1.jar" Main
```

### 3. Run Frontend
```powershell
cd frontend
npm run start
# Or for live hot-reload development:
npm run dev
```
Open **[http://localhost:3000](http://localhost:3000)** in your browser!
