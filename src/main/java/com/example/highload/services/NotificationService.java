package com.example.highload.services;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.network.NotificationDto;

import java.util.List;

public interface NotificationService {

    NotificationDto saveNotification(NotificationDto notificationDto);

    NotificationDto updateNotification(NotificationDto notificationDto, int id);

    List<NotificationDto> getUserNotifications(int userId);

}
