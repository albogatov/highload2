package com.example.highload.services.impl;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.network.NotificationDto;
import com.example.highload.repos.NotificationRepository;
import com.example.highload.services.NotificationService;
import com.example.highload.utils.DataTransformer;
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
        return notificationRepository.findAllByReceiverProfile_Id(userId, pageable);
    }

    @Override
    public Page<Notification> getNewUserNotifications(int userId, Pageable pageable) {
        return notificationRepository.findAllByIsReadFalseAndReceiverProfile_Id(userId, pageable);
    }
}
