package com.ordermonitor.repository;

import com.ordermonitor.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Get all orders for a specific subscriber */
    List<Order> findByUserIdOrderByPlacedAtDesc(Long userId);

    /** Count by a specific order status */
    long countByOrderStatus(String status);

    /** Orders that are PLACED or PAID but NOT shipped within 24h */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('PLACED','PAID') AND o.placedAt < :cutoff")
    List<Order> findDelayedShipments(@Param("cutoff") LocalDateTime cutoff);

    /** Orders SHIPPED but not DELIVERED within 48h of shipping */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'SHIPPED' AND o.shippedAt < :cutoff")
    List<Order> findDelayedDeliveries(@Param("cutoff") LocalDateTime cutoff);

    /** Active orders (PLACED, PAID, SHIPPED) for a user */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderStatus IN ('PLACED','PAID','SHIPPED') ORDER BY o.placedAt DESC")
    List<Order> findActiveOrdersByUser(@Param("userId") Long userId);

    /** Completed orders for a user */
    List<Order> findByUserIdAndOrderStatusOrderByUpdatedAtDesc(Long userId, String status);

    /** All orders ordered by newest first */
    List<Order> findAllByOrderByPlacedAtDesc();
}
