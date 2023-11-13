package com.example.highload.controllers;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Review;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.NotificationRepository;
import com.example.highload.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/review/")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(notificationRepository.save(notificationService.prepareEntity(data)) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save review, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
        List<Notification> entityList = notificationRepository.findAll();
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single")
    public ResponseEntity getById(@RequestBody IdDto data){
        Order entity = notificationRepository.findById(data.getId()).get();
        return ResponseEntity.ok(notificationService.prepareDto(entity));
    }
}
