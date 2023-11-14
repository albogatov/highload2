package com.example.highload.controllers;

import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Review;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.model.network.ReviewDto;
import com.example.highload.repos.ReviewRepository;
import com.example.highload.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/review/")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @CrossOrigin
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('CLIENT')")
    public ResponseEntity save(@RequestBody ReviewDto data){
        if(reviewService.saveReview(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save review, check data");
    }

    @CrossOrigin
    @GetMapping("/all/{profileId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllQueriesProfile(@PathVariable int profileId){
        List<ReviewDto> entityList = reviewService.findAllProfileReviews(profileId);
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@PathVariable int id){
        ReviewDto entity = reviewService.findById(id);
        return ResponseEntity.ok(entity);
    }
}
