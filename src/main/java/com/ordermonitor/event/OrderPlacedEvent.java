package com.ordermonitor.event;

import com.ordermonitor.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Fired when a subscriber places a new order. */
@Getter
public class OrderPlacedEvent extends ApplicationEvent {

    private final Order order;

    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
