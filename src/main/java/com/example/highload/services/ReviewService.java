package com.example.highload.services;

import com.example.highload.model.network.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<ReviewDto> findAllProfileReviews(int profileId, Pageable pageable);

    ReviewDto findById(int id);

    ReviewDto saveReview(ReviewDto data);

}
