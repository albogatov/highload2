package com.example.highload.controllers;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.network.NotificationDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.services.NotificationService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/notification/")
@RequiredArgsConstructor
public class NotificationController {

    private NotificationService notificationService;
    private PaginationHeadersCreator paginationHeadersCreator;
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
    @GetMapping("/all/{userId}/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllQueries(@PathVariable int userId, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<Notification> entityList = notificationService.getAllUserNotifications(userId,pageable);
        List<NotificationDto> dtoList = dataTransformer.notificationListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }


    @CrossOrigin
    @GetMapping("/new/{userId}/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getNewQueries(@PathVariable int userId, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<Notification> entityList = notificationService.getNewUserNotifications(userId, pageable);
        List<NotificationDto> dtoList = dataTransformer.notificationListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }
}
