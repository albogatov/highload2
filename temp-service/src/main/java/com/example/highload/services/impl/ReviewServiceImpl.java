package com.example.highload.services.impl;

import com.example.highload.model.inner.Review;
import com.example.highload.model.network.ReviewDto;
import com.example.highload.repos.ReviewRepository;
import com.example.highload.services.ReviewService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final DataTransformer dataTransformer;

    @Override
    public Page<Review> findAllProfileReviews(int profileId, Pageable pageable) {
        return reviewRepository.findAllByProfile_Id(profileId, pageable).orElse(Page.empty());
    }

    @Override
    public Review findById(int id) {
        return reviewRepository.findById(id).orElse(null);
    }

    @Override
    public Review saveReview(ReviewDto reviewDto) {
        return reviewRepository.save(dataTransformer.reviewFromDto(reviewDto));
    }
}
