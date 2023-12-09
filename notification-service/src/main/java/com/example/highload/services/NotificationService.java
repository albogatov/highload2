package com.example.highload.services;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.network.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Notification saveNotification(NotificationDto notificationDto);

    Notification readNotification(int id);

    Page<Notification> getAllUserNotifications(int userId, Pageable pageable);

    Page<Notification> getNewUserNotifications(int userId, Pageable pageable);

}
