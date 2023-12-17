package com.example.image.utils;

import com.example.highload.model.inner.*;
import com.example.highload.model.network.*;
import com.example.highload.repos.*;
import com.example.image.model.network.ImageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dataTransformer")
@Data
@AllArgsConstructor
public class DataTransformer {

    private final ImageRepository imageRepository;


    /* images */

    public ImageDto imageToDto(Image image) {
        ImageDto imageDto = new ImageDto();
        imageDto.setId(image.getId());
        imageDto.setUrl(image.getUrl());
        return imageDto;
    }

    public Image imageFromDto(ImageDto imageDto) {
        Image image = new Image();
        image.setId(imageDto.getId());
        image.setUrl(imageDto.getUrl());
        return image;
    }

    public List<ImageDto> imageListToDto(List<Image> entities) {
        return entities.stream().map(this::imageToDto).toList();
    }

}
