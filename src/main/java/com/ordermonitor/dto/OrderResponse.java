package com.ordermonitor.dto;

import com.ordermonitor.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only view of an Order sent to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /** Map entity → DTO */
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
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
}
