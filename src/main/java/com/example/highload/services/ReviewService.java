package com.example.highload.services;

import com.example.highload.model.inner.Review;
import com.example.highload.model.network.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<Review> findAllProfileReviews(int profileId, Pageable pageable);

    Review findById(int id);

    Review saveReview(ReviewDto reviewDto);

}
