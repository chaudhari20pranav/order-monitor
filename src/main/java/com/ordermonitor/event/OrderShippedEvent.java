package com.ordermonitor.event;

import com.ordermonitor.entity.Order;
import org.springframework.context.ApplicationEvent;

/** Fired when admin marks an order as SHIPPED. */
public class OrderShippedEvent extends ApplicationEvent {

    private final Order order;

    public OrderShippedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order getOrder() { return order; }
}