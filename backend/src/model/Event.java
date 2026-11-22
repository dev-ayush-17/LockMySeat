package model;

import java.sql.Timestamp;

/**
 * Event entity representing a bookable event in a specific city/venue.
 */
public class Event extends BaseEntity {
    private String title;
    private String description;
    private String city;
    private String venue;
    private Timestamp eventDate;
    private int totalSeats;
    private int availableSeats;

    public Event() {
        super();
        this.totalSeats = 30;
    }

    public Event(Long id, String title, String description, String city, String venue, 
                 Timestamp eventDate, int totalSeats, int availableSeats, Timestamp createdAt) {
        super(id, createdAt);
        this.title = title;
        this.description = description;
        this.city = city;
        this.venue = venue;
        this.eventDate = eventDate;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Timestamp getEventDate() { return eventDate; }
    public void setEventDate(Timestamp eventDate) { this.eventDate = eventDate; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
