package com.example.highload.utils;

import com.example.highload.model.inner.User;
import com.example.highload.services.NotificationService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("notificationConsistencyChecker")
@RequiredArgsConstructor
public class NotificationConsistencyChecker {

    private final NotificationService notificationService;

    public boolean mayReadNotification(@Nonnull final UserDetails principal, final int notificationId) {
        return Objects.equals(notificationService.readNotification(notificationId).getReceiverProfile().getId(), ((User) principal).getId());
    }

}
