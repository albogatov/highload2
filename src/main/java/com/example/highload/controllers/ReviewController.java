package com.example.highload.controllers;

import com.example.highload.model.network.ReviewDto;
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
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getAllByProfile(@PathVariable int profileId){
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
