package com.example.highload.controllers;

import com.example.highload.model.network.ImageDto;
import com.example.highload.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/image")
@RequiredArgsConstructor
public class ImageObjectController {

    private final ImageService imageService;

    @CrossOrigin
    @PostMapping("/add/order/{orderId}")
    public ResponseEntity addImagesToOrder(@RequestBody List<ImageDto> imageDtos, @PathVariable int orderId){
        if (imageService.saveImagesForOrder(imageDtos, orderId) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save image to the order, check data");
    }

    @CrossOrigin
    @PostMapping("/add/profile/{profileId}")
    public ResponseEntity addImagesToProfile(@RequestBody List<ImageDto> imageDtos, @PathVariable int profileId){
        if (imageService.saveImageForProfile(imageDtos, profileId) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save image to the profile, check data");
    }

    @CrossOrigin
    @PostMapping("/change/profile/{profileId}")
    public ResponseEntity changeMainImageOfProfile(@RequestBody ImageDto imageDto, @PathVariable int profileId){
        if (imageService.changeMainImageOfProfile(imageDto, profileId) != null)
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

}
