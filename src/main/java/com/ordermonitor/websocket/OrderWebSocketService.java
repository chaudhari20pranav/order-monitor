package com.ordermonitor.websocket;

import com.ordermonitor.dto.WsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcasts real-time messages to all WebSocket subscribers.
 *
 * /topic/orders        – full order update payloads (admin + subscriber)
 * /topic/notifications – lightweight notification alerts
 */
@Service
public class OrderWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(OrderWebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public OrderWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** Broadcast a full notification to all connected clients */
    public void broadcastToOrders(WsNotification notification) {
        messagingTemplate.convertAndSend("/topic/orders", notification);
        log.info("WS broadcast → /topic/orders | type={}", notification.getType());
    }

    /** Broadcast a lightweight alert to the notifications panel */
    public void broadcastNotification(WsNotification notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
        log.info("WS broadcast → /topic/notifications | type={}", notification.getType());
    }
}