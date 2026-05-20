package com.ordermonitor.event;

import com.ordermonitor.entity.User;
import org.springframework.context.ApplicationEvent;

/** Fired by the scheduler when an admin has been inactive for 8+ hours. */
public class ReminderGeneratedEvent extends ApplicationEvent {

    private final User admin;
    private final String message;

    public ReminderGeneratedEvent(Object source, User admin, String message) {
        super(source);
        this.admin = admin;
        this.message = message;
    }

    public User getAdmin()     { return admin; }
    public String getMessage() { return message; }
}