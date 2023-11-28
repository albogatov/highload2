package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.services.ImageService;
import com.example.highload.services.ProfileService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
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

    private final ProfileService profileService;
    private final ImageService imageService;
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

    @CrossOrigin
    @GetMapping("/single/{id}/images/{page}")
    public ResponseEntity getProfileImagesByIdAndPageNumber(@PathVariable int id, @PathVariable int page){
        Profile entity = profileService.findById(id);
        Pageable pageable = PageRequest.of(page, 50);
        Page<Image> images = imageService.findAllProfileImages(id, pageable);
        // TODO вынести 50 в константы из хардкода
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(images);
        // "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
        return ResponseEntity.ok().headers(responseHeaders).body(dataTransformer.imageListToDto(images.getContent()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(){
        return ResponseEntity.badRequest().body("Request body validation failed!");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity handleServiceExceptions(){
        return ResponseEntity.badRequest().body("Wrong ids in path!");
    }

}
