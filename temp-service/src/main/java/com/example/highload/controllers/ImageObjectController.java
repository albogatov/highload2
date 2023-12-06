package com.example.highload.controllers;

import com.example.highload.model.network.ImageDto;
import com.example.highload.services.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/app/image")
@RequiredArgsConstructor
public class ImageObjectController {

    private final ImageService imageService;

    @CrossOrigin
    @PreAuthorize("hasAuthority('CLIENT')")
    @PostMapping("/add/order/{orderId}")
    public ResponseEntity addImagesToOrder(@Valid @RequestBody List<ImageDto> imageDtos, @PathVariable int orderId) {
        imageService.saveImagesForOrder(imageDtos, orderId);
        return ResponseEntity.ok("Images added");
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ARTIST')")
    @PostMapping("/add/profile/{profileId}")
    public ResponseEntity addImagesToProfile(@Valid @RequestBody List<ImageDto> imageDtos, @PathVariable int profileId) {
        imageService.saveImageForProfile(imageDtos, profileId);
        return ResponseEntity.ok("Images added");
    }

    @CrossOrigin
    @PostMapping("/change/profile/{profileId}")
    public ResponseEntity changeMainImageOfProfile(@Valid @RequestBody ImageDto imageDto, @PathVariable int profileId) {
        imageService.changeMainImageOfProfile(imageDto, profileId);
        return ResponseEntity.ok("Main image changed");
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('CLIENT')")
    @PostMapping("/remove/order/{orderId}/{imageId}")
    public ResponseEntity removeImageForOrder(@PathVariable int imageId, @PathVariable int orderId) {
        imageService.removeImageForOrder(imageId, orderId);
        return ResponseEntity.ok("Image removed");
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ARTIST')")
    @PostMapping("/remove/profile/{profileId}/{imageId}")
    public ResponseEntity removeImageForProfile(@PathVariable int imageId, @PathVariable int profileId) {
        imageService.removeImageForProfile(imageId, profileId);
        return ResponseEntity.ok("Image removed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions() {
        return ResponseEntity.badRequest().body("Request body validation failed!");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity handleServiceExceptions() {
        return ResponseEntity.badRequest().body("Wrong ids in path!");
    }

}
