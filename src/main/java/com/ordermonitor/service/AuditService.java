package com.ordermonitor.service;

import com.ordermonitor.entity.OrderEvent;
import com.ordermonitor.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Writes audit events to the order_events table.
 * Used to power the admin live activity feed.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final OrderEventRepository orderEventRepository;

    @Transactional
    public void log(Long orderId, String eventType, String message) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .eventType(eventType)
                .message(message)
                .build();
        orderEventRepository.save(event);
    }

    public List<OrderEvent> getRecentEvents() {
        return orderEventRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public List<OrderEvent> getEventsForOrder(Long orderId) {
        return orderEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
