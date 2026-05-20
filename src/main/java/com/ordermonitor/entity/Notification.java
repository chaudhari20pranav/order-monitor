package com.ordermonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * In-app notification stored in the database.
 * Subscribers see their own; admins see all via WebSocket.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user this notification belongs to (null = broadcast to admins) */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Long id, Long userId, String message,
                        Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRead == null) this.isRead = false;
    }

    // ---------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long userId;
        private String message;
        private Boolean isRead;
        private LocalDateTime createdAt;

        public Builder id(Long id)               { this.id = id; return this; }
        public Builder userId(Long userId)       { this.userId = userId; return this; }
        public Builder message(String message)   { this.message = message; return this; }
        public Builder isRead(Boolean isRead)    { this.isRead = isRead; return this; }
        public Builder createdAt(LocalDateTime v){ this.createdAt = v; return this; }

        public Notification build() {
            return new Notification(id, userId, message, isRead, createdAt);
        }
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Long getUserId()                    { return userId; }
    public void setUserId(Long userId)         { this.userId = userId; }

    public String getMessage()                 { return message; }
    public void setMessage(String message)     { this.message = message; }

    public Boolean getIsRead()                 { return isRead; }
    public void setIsRead(Boolean isRead)      { this.isRead = isRead; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
}