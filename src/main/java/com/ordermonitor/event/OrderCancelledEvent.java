package com.ordermonitor.event;

import com.ordermonitor.entity.Order;
import org.springframework.context.ApplicationEvent;

/** Fired when an order is cancelled (by subscriber or admin). */
public class OrderCancelledEvent extends ApplicationEvent {

    private final Order order;

    public OrderCancelledEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order getOrder() { return order; }
}