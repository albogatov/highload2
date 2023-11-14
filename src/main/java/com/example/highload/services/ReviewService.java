package com.example.highload.services;

import com.example.highload.model.network.ReviewDto;

import java.util.List;

public interface ReviewService {

    List<ReviewDto> findAllProfileReviews(int profileId);

    ReviewDto findById(int id);

    ReviewDto saveReview(ReviewDto data);

}
