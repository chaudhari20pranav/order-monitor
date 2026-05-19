package com.ordermonitor.service;

import com.ordermonitor.dto.OrderRequest;
import com.ordermonitor.dto.OrderResponse;
import com.ordermonitor.entity.Order;
import com.ordermonitor.repository.OrderRepository;
import com.ordermonitor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 * All dependencies are mocked – no Spring context or DB required.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, userRepository, eventPublisher);
    }

    // ---------------------------------------------------------------
    // placeOrder
    // ---------------------------------------------------------------

    @Test
    @DisplayName("placeOrder: should create order with PLACED status and PENDING payment")
    void placeOrder_shouldCreateOrderCorrectly() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setProductName("aapl");     // should be uppercased
        request.setCategory("Electronics");
        request.setQuantity(50);
        request.setPrice(new BigDecimal("175.50"));

        Order savedOrder = Order.builder()
                .id(1L)
                .userId(10L)
                .productName("Wireless Headphones")
                .category("Electronics")
                .quantity(50)
                .price(new BigDecimal("175.50"))
                .paymentStatus("PENDING")
                .orderStatus("PLACED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.placeOrder(10L, request);

        // Assert
        assertThat(response.getOrderStatus()).isEqualTo("PLACED");
        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(response.getProductName()).isEqualTo("Wireless Headphones");

        // Verify symbol was uppercased before save
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getProductName()).isEqualTo("Wireless Headphones");

        // Verify event was published
        verify(eventPublisher).publishEvent(any());
    }

    // ---------------------------------------------------------------
    // payOrder
    // ---------------------------------------------------------------

    @Test
    @DisplayName("payOrder: should update status to PAID when order is PLACED")
    void payOrder_shouldUpdateToPaid() {
        Order existingOrder = Order.builder()
                .id(1L).userId(10L)
                .productName("Running Shoes")
                .category("Electronics")
                .quantity(10)
                .price(new BigDecimal("200.00"))
                .paymentStatus("PENDING")
                .orderStatus("PLACED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order paidOrder = Order.builder()
                .id(1L).userId(10L)
                .productName("Running Shoes")
                .category("Electronics")
                .quantity(10)
                .price(new BigDecimal("200.00"))
                .paymentStatus("COMPLETED")
                .orderStatus("PAID")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any())).thenReturn(paidOrder);

        OrderResponse response = orderService.payOrder(1L, 10L);

        assertThat(response.getOrderStatus()).isEqualTo("PAID");
        assertThat(response.getPaymentStatus()).isEqualTo("COMPLETED");
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("payOrder: should throw exception when order is not PLACED")
    void payOrder_shouldThrowWhenNotPlaced() {
        Order order = Order.builder()
                .id(1L).userId(10L)
                .orderStatus("SHIPPED")
                .paymentStatus("COMPLETED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payOrder(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PLACED orders can be paid");
    }

    // ---------------------------------------------------------------
    // cancelOrder
    // ---------------------------------------------------------------

    @Test
    @DisplayName("cancelOrder: should cancel a PLACED order")
    void cancelOrder_shouldCancelSuccessfully() {
        Order order = Order.builder()
                .id(2L).userId(10L)
                .productName("Coffee Maker")
                .category("Home & Kitchen")
                .quantity(5)
                .price(new BigDecimal("300.00"))
                .paymentStatus("PENDING")
                .orderStatus("PLACED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order cancelled = Order.builder()
                .id(2L).userId(10L)
                .productName("Coffee Maker")
                .category("Home & Kitchen")
                .quantity(5)
                .price(new BigDecimal("300.00"))
                .paymentStatus("PENDING")
                .orderStatus("CANCELLED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(cancelled);

        OrderResponse response = orderService.cancelOrder(2L, 10L);
        assertThat(response.getOrderStatus()).isEqualTo("CANCELLED");
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("cancelOrder: should throw when order belongs to different user")
    void cancelOrder_shouldThrowOnWrongUser() {
        Order order = Order.builder()
                .id(3L).userId(99L)  // belongs to user 99
                .orderStatus("PLACED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        // User 10 trying to cancel user 99's order
        assertThatThrownBy(() -> orderService.cancelOrder(3L, 10L))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("cancelOrder: should throw when order is already DELIVERED")
    void cancelOrder_shouldThrowWhenDelivered() {
        Order order = Order.builder()
                .id(4L).userId(10L)
                .orderStatus("DELIVERED")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(4L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    // ---------------------------------------------------------------
    // adminUpdateStatus
    // ---------------------------------------------------------------

    @Test
    @DisplayName("adminUpdateStatus: SHIPPED transition should set shippedAt timestamp")
    void adminUpdateStatus_shouldSetShippedAt() {
        Order order = Order.builder()
                .id(5L).userId(10L)
                .productName("Laptop Stand")
                .category("Electronics")
                .quantity(1)
                .price(new BigDecimal("180.00"))
                .paymentStatus("COMPLETED")
                .orderStatus("PAID")
                .placedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.adminUpdateStatus(5L, "SHIPPED");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getShippedAt()).isNotNull();
        assertThat(captor.getValue().getOrderStatus()).isEqualTo("SHIPPED");
    }

    @Test
    @DisplayName("adminUpdateStatus: DELIVERED transition should set deliveredAt timestamp")
    void adminUpdateStatus_shouldSetDeliveredAt() {
        Order order = Order.builder()
                .id(6L).userId(10L)
                .productName("Smart Watch")
                .category("Electronics")
                .quantity(2)
                .price(new BigDecimal("140.00"))
                .paymentStatus("COMPLETED")
                .orderStatus("SHIPPED")
                .placedAt(LocalDateTime.now())
                .shippedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.adminUpdateStatus(6L, "DELIVERED");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getDeliveredAt()).isNotNull();
        assertThat(captor.getValue().getOrderStatus()).isEqualTo("DELIVERED");
    }
}
