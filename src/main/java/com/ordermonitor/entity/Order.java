package com.ordermonitor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a product order placed by a customer.
 *
 * Status flow: PLACED → PAID → SHIPPED → DELIVERED (or CANCELLED at any point).
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to users.id – the customer who placed the order */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /** "PENDING" or "COMPLETED" */
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    /** PLACED | PAID | SHIPPED | DELIVERED | CANCELLED */
    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Order() {}

    public Order(Long id, Long userId, String productName, String category,
                 Integer quantity, BigDecimal price, String paymentStatus, String orderStatus,
                 LocalDateTime placedAt, LocalDateTime shippedAt,
                 LocalDateTime deliveredAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.placedAt = placedAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onCreate() {
        this.placedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long userId;
        private String productName;
        private String category;
        private Integer quantity;
        private BigDecimal price;
        private String paymentStatus;
        private String orderStatus;
        private LocalDateTime placedAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id)                        { this.id = id; return this; }
        public Builder userId(Long userId)                { this.userId = userId; return this; }
        public Builder productName(String v)              { this.productName = v; return this; }
        public Builder category(String v)                 { this.category = v; return this; }
        public Builder quantity(Integer v)                { this.quantity = v; return this; }
        public Builder price(BigDecimal v)                { this.price = v; return this; }
        public Builder paymentStatus(String v)            { this.paymentStatus = v; return this; }
        public Builder orderStatus(String v)              { this.orderStatus = v; return this; }
        public Builder placedAt(LocalDateTime v)          { this.placedAt = v; return this; }
        public Builder shippedAt(LocalDateTime v)         { this.shippedAt = v; return this; }
        public Builder deliveredAt(LocalDateTime v)       { this.deliveredAt = v; return this; }
        public Builder updatedAt(LocalDateTime v)         { this.updatedAt = v; return this; }

        public Order build() {
            return new Order(id, userId, productName, category, quantity, price,
                    paymentStatus, orderStatus, placedAt, shippedAt, deliveredAt, updatedAt);
        }
    }

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public Long getUserId()                      { return userId; }
    public void setUserId(Long userId)           { this.userId = userId; }

    public String getProductName()               { return productName; }
    public void setProductName(String v)         { this.productName = v; }

    public String getCategory()                  { return category; }
    public void setCategory(String v)            { this.category = v; }

    public Integer getQuantity()                 { return quantity; }
    public void setQuantity(Integer v)           { this.quantity = v; }

    public BigDecimal getPrice()                 { return price; }
    public void setPrice(BigDecimal v)           { this.price = v; }

    public String getPaymentStatus()             { return paymentStatus; }
    public void setPaymentStatus(String v)       { this.paymentStatus = v; }

    public String getOrderStatus()               { return orderStatus; }
    public void setOrderStatus(String v)         { this.orderStatus = v; }

    public LocalDateTime getPlacedAt()           { return placedAt; }
    public void setPlacedAt(LocalDateTime v)     { this.placedAt = v; }

    public LocalDateTime getShippedAt()          { return shippedAt; }
    public void setShippedAt(LocalDateTime v)    { this.shippedAt = v; }

    public LocalDateTime getDeliveredAt()        { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime v)  { this.deliveredAt = v; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)    { this.updatedAt = v; }
}