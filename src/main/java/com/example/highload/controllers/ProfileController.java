package com.example.highload.controllers;

import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.services.ImageService;
import com.example.highload.services.ProfileService;
import com.example.highload.utils.PaginationHeadersCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/profile/")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PaginationHeadersCreator paginationHeadersCreator;

    @CrossOrigin
    @PostMapping("/edit/{id}")
    public ResponseEntity edit(@RequestBody ProfileDto data, @PathVariable int id){
        if (profileService.editProfile(data, id) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save profile changes, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getAll(){
        List<ProfileDto> entityList = profileService.findAllProfiles();
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single/{id}")
    public ResponseEntity getById(@PathVariable int id){
        ProfileDto entity = profileService.findById(id);
        return ResponseEntity.ok(entity);
    }

    @CrossOrigin
    @GetMapping("/single/{id}/images/{page}")
    public ResponseEntity getProfileImagesByIdAndPageNumber(@PathVariable int id, @PathVariable int page){

        Pageable pageable = PageRequest.of(page, 50);
        Page<ImageDto> images = imageService.findAllProfileImages(id, pageable);
        // TODO вынести 50 в константы из хардкода
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(images);
        // "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
        return ResponseEntity.ok().headers(responseHeaders).body(images.getContent());

    }
}
