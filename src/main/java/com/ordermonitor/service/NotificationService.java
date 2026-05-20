package com.ordermonitor.service;

import com.ordermonitor.entity.Notification;
import com.ordermonitor.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Saves and retrieves in-app notifications from the database.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** Save a notification for a specific user */
    @Transactional
    public void createNotification(Long userId, String message) {
        Notification n = Notification.builder()
                .userId(userId)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(n);
    }

    /** Get all notifications for a subscriber */
    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Get latest 20 notifications (admin activity feed) */
    public List<Notification> getRecent() {
        return notificationRepository.findTop20ByOrderByCreatedAtDesc();
    }

    /** Unread count for a user */
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    /** Mark all as read for a user */
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }
}