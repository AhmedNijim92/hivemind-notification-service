package com.hivemind.notification.service;

import com.hivemind.notification.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface INotificationService
{
    Notification createNotification(UUID userId, String type, String title, String message, UUID referenceId);

    List<Notification> getNotifications(UUID userId);

    List<Notification> getUnreadNotifications(UUID userId);

    long getUnreadCount(UUID userId);

    void markAsRead(String notificationId);

    void markAllAsRead(UUID userId);
}
