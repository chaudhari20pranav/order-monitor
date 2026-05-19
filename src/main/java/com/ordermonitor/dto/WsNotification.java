package com.ordermonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload broadcast over WebSocket to all subscribers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsNotification {

    /** e.g. ORDER_PLACED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, REMINDER */
    private String type;

    /** Human-readable message shown in the notification panel */
    private String message;

    /** The order involved (may be null for reminder events) */
    private OrderResponse order;

    /** For targeted subscriber notifications, the userId */
    private Long targetUserId;
}
