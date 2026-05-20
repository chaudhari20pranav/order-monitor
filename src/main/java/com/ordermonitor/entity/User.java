package com.ordermonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a registered platform user (ADMIN or SUBSCRIBER).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    /** Either "ADMIN" or "SUBSCRIBER" */
    @Column(name = "role", nullable = false)
    private String role;

    /** Updated on every dashboard visit – used to detect inactivity */
    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String fullName, String email, String password,
                String role, LocalDateTime lastActive, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.lastActive = lastActive;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
    }

    // ---------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String fullName;
        private String email;
        private String password;
        private String role;
        private LocalDateTime lastActive;
        private LocalDateTime createdAt;

        public Builder id(Long id)                       { this.id = id; return this; }
        public Builder fullName(String fullName)         { this.fullName = fullName; return this; }
        public Builder email(String email)               { this.email = email; return this; }
        public Builder password(String password)         { this.password = password; return this; }
        public Builder role(String role)                 { this.role = role; return this; }
        public Builder lastActive(LocalDateTime v)       { this.lastActive = v; return this; }
        public Builder createdAt(LocalDateTime v)        { this.createdAt = v; return this; }

        public User build() {
            return new User(id, fullName, email, password, role, lastActive, createdAt);
        }
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public Long getId()                       { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getFullName()              { return fullName; }
    public void setFullName(String v)        { this.fullName = v; }

    public String getEmail()                 { return email; }
    public void setEmail(String v)           { this.email = v; }

    public String getPassword()              { return password; }
    public void setPassword(String v)        { this.password = v; }

    public String getRole()                  { return role; }
    public void setRole(String v)            { this.role = v; }

    public LocalDateTime getLastActive()     { return lastActive; }
    public void setLastActive(LocalDateTime v) { this.lastActive = v; }

    public LocalDateTime getCreatedAt()      { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}