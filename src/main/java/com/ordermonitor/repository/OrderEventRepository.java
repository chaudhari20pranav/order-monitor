package com.ordermonitor.repository;

import com.ordermonitor.entity.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findTop20ByOrderByCreatedAtDesc();

    List<OrderEvent> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
