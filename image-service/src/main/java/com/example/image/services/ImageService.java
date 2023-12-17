package com.example.image.services;

import com.example.image.model.network.ImageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ImageService {

    Page<Image> findAllProfileImages(int profileId, Pageable pageable);

    Page<Image> findAllOrderImages(int orderId, Pageable pageable);

    Image saveImage(ImageDto imageDto);

    List<Image> saveImagesForOrder(List<ImageDto> imageDtos, int orderId);

    List<Image> saveImageForProfile(List<ImageDto> imageDtos, int profileId);

    void removeImageForOrder(int imageId, int orderId);

    void removeImageForProfile(int imageId, int profileId);

    Image changeMainImageOfProfile(ImageDto imageDto, int profileId);

}
