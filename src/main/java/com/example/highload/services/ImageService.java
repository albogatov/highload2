package com.example.highload.services;

import com.example.highload.model.inner.Image;
import com.example.highload.model.network.ImageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ImageService {

    Page<Image> findAllProfileImages(int profileId, Pageable pageable);

    Page<Image> findAllOrderImages(int orderId, Pageable pageable);

    Image saveImage(ImageDto imageDto);

//    Page<ImageDto> getPage(List<ImageDto> images, Pageable pageable);
}
