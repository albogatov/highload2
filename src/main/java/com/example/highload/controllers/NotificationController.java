package com.example.highload.controllers;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Review;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.NotificationDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.NotificationRepository;
import com.example.highload.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/notification/")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody NotificationDto data){
        if(notificationService.saveNotification(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save notification, check data");
    }

    @CrossOrigin
    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST') and notificationConsistencyChecker.mayReadNotification(authentication.principal, #id)")
    public ResponseEntity setRead(@PathVariable int id){
        if(notificationService.readNotification(id) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't change notification, check data");
    }

    @CrossOrigin
    @GetMapping("/all/{userId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllQueries(@PathVariable int userId) {
        List<NotificationDto> entityList = notificationService.getAllUserNotifications(userId);
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/new/{userId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getNewQueries(@PathVariable int userId) {
        List<NotificationDto> entityList = notificationService.getNewUserNotifications(userId);
        return ResponseEntity.ok(entityList);
    }
}
