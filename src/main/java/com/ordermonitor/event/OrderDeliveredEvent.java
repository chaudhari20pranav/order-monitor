package com.ordermonitor.event;

import com.ordermonitor.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Fired when admin marks an order as DELIVERED. */
@Getter
public class OrderDeliveredEvent extends ApplicationEvent {

    private final Order order;

    public OrderDeliveredEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
