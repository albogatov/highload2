package com.example.highload.repos;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    Page<Image> findAllByImageObject_Order(Order order, Pageable pageable);
    Page<Image> findAllByImageObject_Profile(Profile profile, Pageable pageable);

    void deleteAllByImageObject_Order(Order order);
    void deleteAllByImageObject_Profile(Profile profile);

}
