package com.example.user.controllers;

import com.example.user.model.inner.Profile;
import com.example.user.model.network.ProfileDto;
import com.example.user.services.ProfileService;
import com.example.user.utils.DataTransformer;
import com.example.user.utils.PaginationHeadersCreator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/app/profile")
@RequiredArgsConstructor
public class ProfileAPIController {

    // TODO: Separate profile and image processing
    // ??
    private final ProfileService profileService;
//    private final ImageService imageService;
    private final PaginationHeadersCreator paginationHeadersCreator;
    private final DataTransformer dataTransformer;

    @CrossOrigin
    @PostMapping("/edit/{id}")
    public ResponseEntity edit(@Valid @RequestBody ProfileDto data, @PathVariable int id){
        profileService.editProfile(data, id);
        return ResponseEntity.ok("Profile edited");
    }

    @CrossOrigin
    @GetMapping("/all/{page}")
    public ResponseEntity getAll(@PathVariable int page){
        Pageable pageable = PageRequest.of(page, 50);
        Page<Profile> entityList = profileService.findAllProfiles(pageable);
        List<ProfileDto> dtoList = dataTransformer.profileListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }

    @CrossOrigin
    @GetMapping("/single/{id}")
    public ResponseEntity getById(@PathVariable int id){
        Profile entity = profileService.findById(id);
        return ResponseEntity.ok(dataTransformer.profileToDto(entity));
    }

    // TODO: Transfer this method to Image service
//    @CrossOrigin
//    @GetMapping("/single/{id}/images/{page}")
//    public ResponseEntity getProfileImagesByIdAndPageNumber(@PathVariable int id, @PathVariable int page){
//        Profile entity = profileService.findById(id);
//        Pageable pageable = PageRequest.of(page, 50);
//        Page<Image> images = imageService.findAllProfileImages(id, pageable);
//        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(images);
//        return ResponseEntity.ok().headers(responseHeaders).body(dataTransformer.imageListToDto(images.getContent()));
//
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(){
        return ResponseEntity.badRequest().body("Request body validation failed!");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity handleServiceExceptions(){
        return ResponseEntity.badRequest().body("Wrong ids in path!");
    }

}
