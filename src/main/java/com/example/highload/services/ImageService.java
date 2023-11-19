package com.example.highload.services;

import com.example.highload.model.inner.Image;
import com.example.highload.model.network.ImageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ImageService {

    Page<ImageDto> findAllProfileImages(int id, Pageable pageable);

//    Page<ImageDto> getPage(List<ImageDto> images, Pageable pageable);
}
