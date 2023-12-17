package com.example.notification.services.impl;

import com.example.notification.model.inner.Notification;
import com.example.notification.model.network.NotificationDto;
import com.example.notification.repos.NotificationRepository;
import com.example.notification.services.NotificationService;
import com.example.notification.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DataTransformer dataTransformer;


    @Override
    public Notification saveNotification(NotificationDto notificationDto) {
        return notificationRepository.save(dataTransformer.notificationFromDto(notificationDto));
    }

    @Override
    public Notification readNotification(int id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return notification;
    }

    @Override
    public Page<Notification> getAllUserNotifications(int userId, Pageable pageable) {
        return notificationRepository.findAllByReceiverProfile_Id(userId, pageable).orElseThrow();
    }

    @Override
    public Page<Notification> getNewUserNotifications(int userId, Pageable pageable) {
        return notificationRepository.findAllByIsReadFalseAndReceiverProfile_Id(userId, pageable).orElseThrow();
    }
}
