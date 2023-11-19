package com.example.highload.controllers;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.network.NotificationDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.services.NotificationService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/notification/")
@RequiredArgsConstructor
public class NotificationController {

    private NotificationService notificationService;
    private final DataTransformer dataTransformer;

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
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getAllQueries(@PathVariable int userId) {
        List<Notification> entityList = notificationService.getAllUserNotifications(userId);
        List<NotificationDto> dtoList = dataTransformer.notificationListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }


    @CrossOrigin
    @GetMapping("/new/{userId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getNewQueries(@PathVariable int userId) {
        List<Notification> entityList = notificationService.getNewUserNotifications(userId);
        List<NotificationDto> dtoList = dataTransformer.notificationListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }
}
