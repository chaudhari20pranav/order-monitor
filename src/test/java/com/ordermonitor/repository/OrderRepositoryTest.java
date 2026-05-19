package com.ordermonitor.repository;

import com.ordermonitor.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository integration test using H2 in-memory database.
 * Tests custom JPQL queries.
 */
@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanup() {
        orderRepository.deleteAll();
    }

    private Order buildOrder(String status, LocalDateTime placedAt, LocalDateTime shippedAt) {
        return Order.builder()
                .userId(1L)
                .productName("Wireless Headphones")
                .category("Electronics")
                .quantity(10)
                .price(new BigDecimal("150.00"))
                .paymentStatus("PENDING")
                .orderStatus(status)
                .placedAt(placedAt)
                .shippedAt(shippedAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findDelayedShipments: returns PLACED orders older than cutoff")
    void findDelayedShipments_shouldReturnOverdueOrders() {
        // Order placed 30 hours ago → delayed (cutoff = 24h)
        Order delayed = buildOrder("PLACED", LocalDateTime.now().minusHours(30), null);
        // Order placed 5 hours ago → not delayed
        Order fresh = buildOrder("PLACED", LocalDateTime.now().minusHours(5), null);
        // SHIPPED order → should NOT appear
        Order shipped = buildOrder("SHIPPED", LocalDateTime.now().minusHours(30), LocalDateTime.now());

        orderRepository.saveAll(List.of(delayed, fresh, shipped));

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Order> result = orderRepository.findDelayedShipments(cutoff);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlacedAt()).isBefore(cutoff);
        assertThat(result.get(0).getOrderStatus()).isEqualTo("PLACED");
    }

    @Test
    @DisplayName("findDelayedDeliveries: returns SHIPPED orders older than 48h cutoff")
    void findDelayedDeliveries_shouldReturnOverdueOrders() {
        // Shipped 50 hours ago → delayed
        Order delayed = buildOrder("SHIPPED", LocalDateTime.now().minusHours(55),
                LocalDateTime.now().minusHours(50));
        // Shipped 10 hours ago → not delayed
        Order fresh = buildOrder("SHIPPED", LocalDateTime.now().minusHours(15),
                LocalDateTime.now().minusHours(10));

        orderRepository.saveAll(List.of(delayed, fresh));

        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        List<Order> result = orderRepository.findDelayedDeliveries(cutoff);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShippedAt()).isBefore(cutoff);
    }

    @Test
    @DisplayName("findByUserIdOrderByPlacedAtDesc: returns only that user's orders")
    void findByUserId_shouldReturnOnlyUserOrders() {
        Order user1Order1 = buildOrder("PLACED", LocalDateTime.now().minusHours(2), null);
        user1Order1.setUserId(1L);

        Order user1Order2 = buildOrder("PAID", LocalDateTime.now().minusHours(1), null);
        user1Order2.setUserId(1L);

        Order user2Order = buildOrder("PLACED", LocalDateTime.now(), null);
        user2Order.setUserId(2L);

        orderRepository.saveAll(List.of(user1Order1, user1Order2, user2Order));

        List<Order> user1Orders = orderRepository.findByUserIdOrderByPlacedAtDesc(1L);

        assertThat(user1Orders).hasSize(2);
        assertThat(user1Orders).allMatch(o -> o.getUserId().equals(1L));
        // Newest first
        assertThat(user1Orders.get(0).getPlacedAt())
                .isAfter(user1Orders.get(1).getPlacedAt());
    }

    @Test
    @DisplayName("countByOrderStatus: returns correct count per status")
    void countByStatus_shouldReturnCorrectCount() {
        orderRepository.save(buildOrder("PLACED", LocalDateTime.now(), null));
        orderRepository.save(buildOrder("PLACED", LocalDateTime.now(), null));
        orderRepository.save(buildOrder("SHIPPED", LocalDateTime.now(), LocalDateTime.now()));

        assertThat(orderRepository.countByOrderStatus("PLACED")).isEqualTo(2);
        assertThat(orderRepository.countByOrderStatus("SHIPPED")).isEqualTo(1);
        assertThat(orderRepository.countByOrderStatus("DELIVERED")).isEqualTo(0);
    }
}
