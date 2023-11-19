package com.example.highload.services.impl;

import com.example.highload.model.inner.Image;
import com.example.highload.model.network.ImageDto;
import com.example.highload.repos.ImageRepository;
import com.example.highload.services.ImageService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final DataTransformer dataTransformer;

    @Override
    public Page<Image> findAllProfileImages(int profileId, Pageable pageable) {
        return imageRepository.findAllByImageObject_Profile_Id(profileId, pageable);
    }

    @Override
    public Page<Image> findAllOrderImages(int orderId, Pageable pageable) {
        return imageRepository.findAllByImageObject_Order_Id(orderId, pageable);
    }

    @Override
    public Image saveImage(ImageDto imageDto) {
        return imageRepository.save(dataTransformer.imageFromDto(imageDto));
    }
}
