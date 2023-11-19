package com.example.highload.utils;

import com.example.highload.model.inner.User;
import com.example.highload.services.NotificationService;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("notificationConsistencyChecker")
public class NotificationConsistencyChecker {

    NotificationService notificationService;

    public boolean mayReadNotification(@Nonnull final UserDetails principal, final int notificationId) {
        return notificationService.readNotification(notificationId).getReceiverId() == ((User)principal).getId();
    }

}
