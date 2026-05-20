package com.ordermonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Immutable audit event for an order.
 * Powers the admin "live activity feed".
 */
@Entity
@Table(name = "order_events")
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** e.g. PLACED, PAID, SHIPPED, DELIVERED, CANCELLED */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public OrderEvent() {}

    public OrderEvent(Long id, Long orderId, String eventType,
                      String message, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.eventType = eventType;
        this.message = message;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long orderId;
        private String eventType;
        private String message;
        private LocalDateTime createdAt;

        public Builder id(Long id)               { this.id = id; return this; }
        public Builder orderId(Long orderId)     { this.orderId = orderId; return this; }
        public Builder eventType(String v)       { this.eventType = v; return this; }
        public Builder message(String message)   { this.message = message; return this; }
        public Builder createdAt(LocalDateTime v){ this.createdAt = v; return this; }

        public OrderEvent build() {
            return new OrderEvent(id, orderId, eventType, message, createdAt);
        }
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Long getOrderId()                   { return orderId; }
    public void setOrderId(Long orderId)       { this.orderId = orderId; }

    public String getEventType()               { return eventType; }
    public void setEventType(String v)         { this.eventType = v; }

    public String getMessage()                 { return message; }
    public void setMessage(String message)     { this.message = message; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
}