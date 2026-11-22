package model;

import java.sql.Timestamp;

/**
 * BaseEntity demonstrates Inheritance and Encapsulation.
 * Abstract base class providing common identity and auditing fields.
 */
public abstract class BaseEntity {
    private Long id;
    private Timestamp createdAt;

    public BaseEntity() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public BaseEntity(Long id, Timestamp createdAt) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
