package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.services.ImageService;
import com.example.highload.services.ProfileService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/image/")
@RequiredArgsConstructor
public class ImageObjectController {

    private ImageService imageService;
    private PaginationHeadersCreator paginationHeadersCreator;
    private final DataTransformer dataTransformer;

    @CrossOrigin
    @PostMapping("/add/order/{id}")
    public ResponseEntity addImageToOrder(@RequestBody ImageDto imageDto, @PathVariable int id){
        if (imageService.saveImageForOrder(imageDto, id) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save image to the order, check data");
    }

    @CrossOrigin
    @PostMapping("/add/profile/{id}")
    public ResponseEntity addImageToProfile(@RequestBody ImageDto imageDto, @PathVariable int id){
        if (imageService.saveImageForProfile(imageDto, id) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save image to the profile, check data");
    }

    @CrossOrigin
    @PostMapping("/remove/order/{orderId}/{imageId}")
    public ResponseEntity removeImageForOrder(@PathVariable int imageId, @PathVariable int orderId){
        imageService.removeImageForOrder(imageId, orderId);
        return ResponseEntity.ok("");
    }

    @CrossOrigin
    @PostMapping("/remove/profile/{profileId}/{imageId}")
    public ResponseEntity removeImageForProfile(@PathVariable int imageId, @PathVariable int profileId){
        imageService.removeImageForProfile(imageId, profileId);
        return ResponseEntity.ok("");
    }


    /*TODO PROFILE IMAGE CHANGE/DELETE WITH IMAGE CONSISTENCY VALIDATION (delete old & set new in transactional in img service)
    may be in ImageController */
}
