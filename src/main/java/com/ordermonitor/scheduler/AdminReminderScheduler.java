package com.ordermonitor.scheduler;

import com.ordermonitor.entity.User;
import com.ordermonitor.event.ReminderGeneratedEvent;
import com.ordermonitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that runs every hour.
 *
 * If an admin has not been active for 8+ hours, a ReminderGeneratedEvent
 * is published which triggers:
 *   - A dashboard WebSocket alert
 *   - An email to that admin
 */
@Component
public class AdminReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdminReminderScheduler.class);
    private static final int INACTIVITY_HOURS = 8;

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminReminderScheduler(UserRepository userRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /** Runs every hour (3600 seconds) */
    @Scheduled(fixedRateString = "PT1H")
    public void checkAdminInactivity() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(INACTIVITY_HOURS);
        List<User> inactiveAdmins = userRepository.findInactiveAdmins(cutoff);

        if (inactiveAdmins.isEmpty()) {
            log.debug("Admin inactivity check: all admins are active.");
            return;
        }

        for (User admin : inactiveAdmins) {
            log.info("Admin {} has been inactive for {}+ hours. Sending reminder.",
                    admin.getEmail(), INACTIVITY_HOURS);
            String message = "⚠️ Reminder: " + admin.getFullName()
                    + " – please check pending order updates in the dashboard.";
            eventPublisher.publishEvent(new ReminderGeneratedEvent(this, admin, message));
        }
    }
}