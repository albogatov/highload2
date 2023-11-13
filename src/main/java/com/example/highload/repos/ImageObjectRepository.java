package com.example.highload.repos;

import com.example.highload.model.inner.ImageObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageObjectRepository extends JpaRepository<ImageObject, Integer> {
}
