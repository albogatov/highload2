package com.example.highload.controllers;

import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Review;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.ReviewRepository;
import com.example.highload.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/review/")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(reviewRepository.save(reviewService.prepareEntity(data)) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save review, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
        List<Review> entityList = reviewRepository.findAll();
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single")
    public ResponseEntity getById(@RequestBody IdDto data){
        Order entity = reviewRepository.findById(data.getId()).get();
        return ResponseEntity.ok(reviewService.prepareDto(entity));
    }
}
