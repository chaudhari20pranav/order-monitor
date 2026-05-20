package com.ordermonitor.dto;

/**
 * Payload broadcast over WebSocket to all subscribers.
 */
public class WsNotification {

    /** e.g. ORDER_PLACED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, REMINDER */
    private String type;

    /** Human-readable message shown in the notification panel */
    private String message;

    /** The order involved (may be null for reminder events) */
    private OrderResponse order;

    /** For targeted subscriber notifications, the userId */
    private Long targetUserId;

    public WsNotification() {}

    public WsNotification(String type, String message, OrderResponse order, Long targetUserId) {
        this.type = type;
        this.message = message;
        this.order = order;
        this.targetUserId = targetUserId;
    }

    public String getType()                  { return type; }
    public void setType(String type)         { this.type = type; }

    public String getMessage()               { return message; }
    public void setMessage(String message)   { this.message = message; }

    public OrderResponse getOrder()          { return order; }
    public void setOrder(OrderResponse order){ this.order = order; }

    public Long getTargetUserId()              { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
}