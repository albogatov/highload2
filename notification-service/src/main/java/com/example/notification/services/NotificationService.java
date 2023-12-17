package com.example.notification.services;

import com.example.notification.model.inner.Notification;
import com.example.notification.model.network.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Notification saveNotification(NotificationDto notificationDto);

    Notification readNotification(int id);

    Page<Notification> getAllUserNotifications(int userId, Pageable pageable);

    Page<Notification> getNewUserNotifications(int userId, Pageable pageable);

}
