package com.example.highload.services;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.network.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    NotificationDto saveNotification(NotificationDto notificationDto);

//    NotificationDto updateNotification(NotificationDto notificationDto, int id);

    NotificationDto readNotification(int id);

    Page<NotificationDto> getAllUserNotifications(int userId, Pageable pageable);

    Page<NotificationDto> getNewUserNotifications(int userId, Pageable pageable);

}
