package com.ordermonitor.listener;

import com.ordermonitor.dto.OrderResponse;
import com.ordermonitor.dto.WsNotification;
import com.ordermonitor.entity.User;
import com.ordermonitor.event.*;
import com.ordermonitor.repository.UserRepository;
import com.ordermonitor.service.AuditService;
import com.ordermonitor.service.EmailService;
import com.ordermonitor.service.NotificationService;
import com.ordermonitor.websocket.OrderWebSocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Central event hub.
 *
 * Every domain event flows through here:
 *   1. Audit log  → order_events table
 *   2. In-app notification → notifications table
 *   3. WebSocket broadcast → /topic/orders + /topic/notifications
 *   4. Email → subscriber's inbox (async)
 */
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final OrderWebSocketService webSocketService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // ---------------------------------------------------------------
    // ORDER PLACED
    // ---------------------------------------------------------------

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        var order = event.getOrder();
        String msg = "Order #" + order.getId() + " placed – " + order.getQuantity() + "x " + order.getProductName();

        auditService.log(order.getId(), "PLACED", msg);
        notificationService.createNotification(order.getUserId(), "✅ " + msg);

        WsNotification ws = new WsNotification("ORDER_PLACED", msg, OrderResponse.from(order), order.getUserId());
        webSocketService.broadcastToOrders(ws);
        webSocketService.broadcastNotification(ws);

        // Email subscriber
        userRepository.findById(order.getUserId()).ifPresent(u ->
                emailService.sendOrderPlacedEmail(u, order));

        log.info("OrderPlacedEvent handled for order #{}", order.getId());
    }

    // ---------------------------------------------------------------
    // PAYMENT COMPLETED
    // ---------------------------------------------------------------

    @EventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        var order = event.getOrder();
        String msg = "Payment completed for Order #" + order.getId() + " – " + order.getProductName();

        auditService.log(order.getId(), "PAID", msg);
        notificationService.createNotification(order.getUserId(), "💳 " + msg);

        WsNotification ws = new WsNotification("PAYMENT_COMPLETED", msg, OrderResponse.from(order), order.getUserId());
        webSocketService.broadcastToOrders(ws);
        webSocketService.broadcastNotification(ws);

        log.info("PaymentCompletedEvent handled for order #{}", order.getId());
    }

    // ---------------------------------------------------------------
    // ORDER SHIPPED
    // ---------------------------------------------------------------

    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        var order = event.getOrder();
        String msg = "Order #" + order.getId() + " shipped – " + order.getProductName();

        auditService.log(order.getId(), "SHIPPED", msg);
        notificationService.createNotification(order.getUserId(), "🚀 " + msg);

        WsNotification ws = new WsNotification("ORDER_SHIPPED", msg, OrderResponse.from(order), order.getUserId());
        webSocketService.broadcastToOrders(ws);
        webSocketService.broadcastNotification(ws);

        // Email subscriber
        userRepository.findById(order.getUserId()).ifPresent(u ->
                emailService.sendOrderShippedEmail(u, order));

        log.info("OrderShippedEvent handled for order #{}", order.getId());
    }

    // ---------------------------------------------------------------
    // ORDER DELIVERED
    // ---------------------------------------------------------------

    @EventListener
    public void onOrderDelivered(OrderDeliveredEvent event) {
        var order = event.getOrder();
        String msg = "Order #" + order.getId() + " delivered – " + order.getProductName();

        auditService.log(order.getId(), "DELIVERED", msg);
        notificationService.createNotification(order.getUserId(), "✅ " + msg);

        WsNotification ws = new WsNotification("ORDER_DELIVERED", msg, OrderResponse.from(order), order.getUserId());
        webSocketService.broadcastToOrders(ws);
        webSocketService.broadcastNotification(ws);

        // Email subscriber
        userRepository.findById(order.getUserId()).ifPresent(u ->
                emailService.sendOrderDeliveredEmail(u, order));

        log.info("OrderDeliveredEvent handled for order #{}", order.getId());
    }

    // ---------------------------------------------------------------
    // ORDER CANCELLED
    // ---------------------------------------------------------------

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        var order = event.getOrder();
        String msg = "Order #" + order.getId() + " cancelled – " + order.getProductName();

        auditService.log(order.getId(), "CANCELLED", msg);
        notificationService.createNotification(order.getUserId(), "❌ " + msg);

        WsNotification ws = new WsNotification("ORDER_CANCELLED", msg, OrderResponse.from(order), order.getUserId());
        webSocketService.broadcastToOrders(ws);
        webSocketService.broadcastNotification(ws);

        // Email subscriber
        userRepository.findById(order.getUserId()).ifPresent(u ->
                emailService.sendOrderCancelledEmail(u, order));

        log.info("OrderCancelledEvent handled for order #{}", order.getId());
    }

    // ---------------------------------------------------------------
    // REMINDER GENERATED
    // ---------------------------------------------------------------

    @EventListener
    public void onReminderGenerated(ReminderGeneratedEvent event) {
        User admin = event.getAdmin();
        String msg = event.getMessage();

        WsNotification ws = new WsNotification("REMINDER", msg, null, null);
        webSocketService.broadcastNotification(ws);

        // Email admin
        emailService.sendAdminReminderEmail(admin);

        log.info("ReminderGeneratedEvent handled for admin: {}", admin.getEmail());
    }
}
