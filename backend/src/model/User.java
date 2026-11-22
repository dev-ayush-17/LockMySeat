package model;

import java.sql.Timestamp;

/**
 * User demonstrates Inheritance (extends BaseEntity) and Encapsulation
 * (private fields with controlled getters/setters).
 */
public class User extends BaseEntity {
    private String name;
    private String email;
    private String passwordHash;
    private String salt;
    private Role role;

    public User() {
        super();
        this.role = Role.USER;
    }

    public User(Long id, String name, String email, String passwordHash, String salt, Role role, Timestamp createdAt) {
        super(id, createdAt);
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role != null ? role : Role.USER;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
