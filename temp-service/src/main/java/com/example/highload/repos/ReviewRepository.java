package com.example.highload.repos;

import com.example.highload.model.inner.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Page<Review> findAllByProfile_Id(Integer id, Pageable pageable);
}
