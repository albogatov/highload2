package com.example.notification.utils;

import com.example.notification.model.inner.Notification;
import com.example.notification.model.network.NotificationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dataTransformer")
@Data
@AllArgsConstructor
public class DataTransformer {

    /* notifications */

    public NotificationDto notificationToDto(Notification notification) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(notification.getId());
        notificationDto.setRead(notification.getIsRead());
        notificationDto.setTime(notification.getTime());
        notificationDto.setReceiverId(notification.getReceiverProfileId());
        notificationDto.setSenderId(notification.getSenderProfileId());
        // TODO ??
        // notificationDto.setSenderMail(notification.getSenderProfileId().getMail());
        return notificationDto;
    }

    public Notification notificationFromDto(NotificationDto notificationDto) {
        Notification notification = new Notification();
        notification.setId(notificationDto.getId());
        notification.setSenderProfileId(notification.getSenderProfileId());
        notification.setReceiverProfileId(notification.getReceiverProfileId());
        notification.setTime(notificationDto.getTime());
        notification.setIsRead(notificationDto.isRead());
        return notification;
    }

}
