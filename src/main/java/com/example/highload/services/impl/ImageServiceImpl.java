package com.example.highload.services.impl;

import com.example.highload.model.enums.ImageObjectType;
import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.ImageObject;
import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.network.ImageDto;
import com.example.highload.repos.ImageObjectRepository;
import com.example.highload.repos.ImageRepository;
import com.example.highload.repos.OrderRepository;
import com.example.highload.repos.ProfileRepository;
import com.example.highload.services.ImageService;
import com.example.highload.utils.DataTransformer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final OrderRepository orderRepository;
    private final ProfileRepository profileRepository;
    private final ImageRepository imageRepository;
    private final ImageObjectRepository imageObjectRepository;
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

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public List<Image> saveImagesForOrder(List<ImageDto> imageDtos, int orderId) {
        ClientOrder order = orderRepository.findById(orderId).orElseThrow();
        List<Image> images = imageRepository.saveAll(imageDtos.stream().map(dataTransformer::imageFromDto).toList());
        List<ImageObject> imageObjects = images.stream().map(image ->
                {
                    ImageObject imageObject = new ImageObject();
                    imageObject.setImage(image);
                    imageObject.setOrder(order);
                    imageObject.setProfile(null);
                    imageObject.setType(ImageObjectType.ORDER_IMAGE);
                    return imageObject;
                }
        ).toList();
        imageObjectRepository.saveAll(imageObjects);
        return images;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public List<Image> saveImageForProfile(List<ImageDto> imageDtos, int profileId) {
        Profile profile = profileRepository.findById(profileId).orElseThrow();
        List<Image> images = imageRepository.saveAll(imageDtos.stream().map(dataTransformer::imageFromDto).toList());
        List<ImageObject> imageObjects = images.stream().map(image ->
                {
                    ImageObject imageObject = new ImageObject();
                    imageObject.setImage(image);
                    imageObject.setOrder(null);
                    imageObject.setProfile(profile);
                    imageObject.setType(ImageObjectType.PROFILE_IMAGE);
                    return imageObject;
                }
        ).toList();
        imageObjectRepository.saveAll(imageObjects);
        return images;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
    public void removeImageForOrder(int imageId, int orderId) {
        imageObjectRepository.deleteByImage_IdAndOrder_Id(imageId, orderId);
        imageRepository.deleteById(imageId);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
    public void removeImageForProfile(int imageId, int profileId) {
        imageObjectRepository.deleteByImage_IdAndProfile_Id(imageId, profileId);
        imageRepository.deleteById(imageId);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public Image changeMainImageOfProfile(ImageDto imageDto, int profileId) {
        Profile profile = profileRepository.findById(profileId).orElseThrow();
        Image oldImage = profile.getImage();
        Image newImage = imageRepository.save(dataTransformer.imageFromDto(imageDto));
        profile.setImage(newImage);
        profileRepository.save(profile);
        if (oldImage != null) {
            imageRepository.deleteById(oldImage.getId());
        }
        return newImage;
    }
}
