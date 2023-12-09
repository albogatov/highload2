package com.example.user.services;

import com.example.user.model.inner.Review;
import com.example.user.model.network.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<Review> findAllProfileReviews(int profileId, Pageable pageable);

    Review findById(int id);

    Review saveReview(ReviewDto reviewDto);

}
