package com.ordermonitor.service;

import com.ordermonitor.dto.AdminDashboardStats;
import com.ordermonitor.dto.OrderRequest;
import com.ordermonitor.dto.OrderResponse;
import com.ordermonitor.entity.Order;
import com.ordermonitor.event.*;
import com.ordermonitor.repository.OrderRepository;
import com.ordermonitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core order business logic.
 *
 * After each DB operation an application event is published.
 * The OrderEventListener picks it up and handles:
 *   - WebSocket broadcast
 *   - Email notification
 *   - Audit log entry
 *   - In-app notification
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // ---------------------------------------------------------------
    // Place Order (customer)
    // ---------------------------------------------------------------

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .productName(request.getProductName())
                .category(request.getCategory())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .paymentStatus("PENDING")
                .orderStatus("PLACED")
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order placed: id={}, user={}, product={}", saved.getId(), userId, saved.getProductName());

        eventPublisher.publishEvent(new OrderPlacedEvent(this, saved));
        return OrderResponse.from(saved);
    }

    // ---------------------------------------------------------------
    // Pay for Order (customer)
    // ---------------------------------------------------------------

    @Transactional
    public OrderResponse payOrder(Long orderId, Long userId) {
        Order order = findOrderForUser(orderId, userId);

        if (!"PLACED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Only PLACED orders can be paid");
        }

        order.setPaymentStatus("COMPLETED");
        order.setOrderStatus("PAID");

        Order saved = orderRepository.save(order);
        log.info("Order paid: id={}", saved.getId());

        eventPublisher.publishEvent(new PaymentCompletedEvent(this, saved));
        return OrderResponse.from(saved);
    }

    // ---------------------------------------------------------------
    // Cancel Order (customer)
    // ---------------------------------------------------------------

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = findOrderForUser(orderId, userId);

        if ("DELIVERED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
            throw new IllegalStateException("This order cannot be cancelled");
        }

        order.setOrderStatus("CANCELLED");
        Order saved = orderRepository.save(order);
        log.info("Order cancelled by customer: id={}", saved.getId());

        eventPublisher.publishEvent(new OrderCancelledEvent(this, saved));
        return OrderResponse.from(saved);
    }

    // ---------------------------------------------------------------
    // Admin: Update Order Status
    // ---------------------------------------------------------------

    @Transactional
    public OrderResponse adminUpdateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        String previous = order.getOrderStatus();
        order.setOrderStatus(newStatus);

        if ("SHIPPED".equals(newStatus)) {
            order.setShippedAt(LocalDateTime.now());
            Order saved = orderRepository.save(order);
            eventPublisher.publishEvent(new OrderShippedEvent(this, saved));
            return OrderResponse.from(saved);
        }

        if ("DELIVERED".equals(newStatus)) {
            order.setDeliveredAt(LocalDateTime.now());
            Order saved = orderRepository.save(order);
            eventPublisher.publishEvent(new OrderDeliveredEvent(this, saved));
            return OrderResponse.from(saved);
        }

        if ("CANCELLED".equals(newStatus)) {
            Order saved = orderRepository.save(order);
            eventPublisher.publishEvent(new OrderCancelledEvent(this, saved));
            return OrderResponse.from(saved);
        }

        Order saved = orderRepository.save(order);
        log.info("Admin updated order {}: {} → {}", orderId, previous, newStatus);
        return OrderResponse.from(saved);
    }

    // ---------------------------------------------------------------
    // Read Queries
    // ---------------------------------------------------------------

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByPlacedAtDesc()
                .stream().map(OrderResponse::from).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersForUser(Long userId) {
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId)
                .stream().map(OrderResponse::from).collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId) {
        return OrderResponse.from(
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId))
        );
    }

    // ---------------------------------------------------------------
    // Admin Dashboard Stats
    // ---------------------------------------------------------------

    public AdminDashboardStats getAdminStats() {
        LocalDateTime ship24hAgo = LocalDateTime.now().minusHours(24);
        LocalDateTime deliver48hAgo = LocalDateTime.now().minusHours(48);

        return new AdminDashboardStats(
                orderRepository.count(),
                orderRepository.countByOrderStatus("PLACED"),
                orderRepository.countByOrderStatus("PAID"),
                orderRepository.countByOrderStatus("SHIPPED"),
                orderRepository.countByOrderStatus("DELIVERED"),
                orderRepository.countByOrderStatus("CANCELLED"),
                orderRepository.findDelayedShipments(ship24hAgo).size(),
                orderRepository.findDelayedDeliveries(deliver48hAgo).size(),
                userRepository.countByRole("SUBSCRIBER")
        );
    }

    // ---------------------------------------------------------------
    // Helper: fetch order and validate ownership
    // ---------------------------------------------------------------

    public Order findOrderForUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to order: " + orderId);
        }
        return order;
    }
}