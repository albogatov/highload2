package com.example.highload.controllers;

import com.example.highload.model.inner.Review;
import com.example.highload.model.network.ReviewDto;
import com.example.highload.services.ReviewService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/review/")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final PaginationHeadersCreator paginationHeadersCreator;
    private final DataTransformer dataTransformer;

    @CrossOrigin
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('CLIENT')")
    public ResponseEntity save(@RequestBody ReviewDto data){
        if(reviewService.saveReview(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save review, check data");
    }

    @CrossOrigin
    @GetMapping("/all/{profileId}/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllByProfile(@PathVariable int profileId, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<Review> entityList = reviewService.findAllProfileReviews(profileId, pageable);
        List<ReviewDto> dtoList = dataTransformer.reviewListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }

    @CrossOrigin
    @GetMapping("/single/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@PathVariable int id){
        Review entity = reviewService.findById(id);
        ReviewDto reviewDto = dataTransformer.reviewToDto(entity);
        return ResponseEntity.ok(reviewDto);
    }
}
