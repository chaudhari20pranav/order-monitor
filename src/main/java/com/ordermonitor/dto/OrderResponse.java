package com.ordermonitor.dto;

import com.ordermonitor.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only view of an Order sent to the frontend.
 */
public class OrderResponse {

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

    public OrderResponse() {}

    public OrderResponse(Long id, Long userId, String productName, String category,
                         Integer quantity, BigDecimal price, String paymentStatus,
                         String orderStatus, LocalDateTime placedAt, LocalDateTime shippedAt,
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

    /** Map entity → DTO */
    public static OrderResponse from(Order order) {
        return new Builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productName(order.getProductName())
                .category(order.getCategory())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .placedAt(order.getPlacedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .updatedAt(order.getUpdatedAt())
                .build();
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

        public Builder id(Long id)                       { this.id = id; return this; }
        public Builder userId(Long userId)               { this.userId = userId; return this; }
        public Builder productName(String v)             { this.productName = v; return this; }
        public Builder category(String v)                { this.category = v; return this; }
        public Builder quantity(Integer v)               { this.quantity = v; return this; }
        public Builder price(BigDecimal v)               { this.price = v; return this; }
        public Builder paymentStatus(String v)           { this.paymentStatus = v; return this; }
        public Builder orderStatus(String v)             { this.orderStatus = v; return this; }
        public Builder placedAt(LocalDateTime v)         { this.placedAt = v; return this; }
        public Builder shippedAt(LocalDateTime v)        { this.shippedAt = v; return this; }
        public Builder deliveredAt(LocalDateTime v)      { this.deliveredAt = v; return this; }
        public Builder updatedAt(LocalDateTime v)        { this.updatedAt = v; return this; }

        public OrderResponse build() {
            return new OrderResponse(id, userId, productName, category, quantity, price,
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