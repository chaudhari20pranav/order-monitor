package com.ordermonitor.event;

import com.ordermonitor.entity.Order;
import org.springframework.context.ApplicationEvent;

/** Fired when a subscriber marks an order as paid. */
public class PaymentCompletedEvent extends ApplicationEvent {

    private final Order order;

    public PaymentCompletedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order getOrder() { return order; }
}