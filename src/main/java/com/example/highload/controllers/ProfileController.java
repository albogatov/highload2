package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.repos.ImageRepository;
import com.example.highload.repos.ProfileRepository;
import com.example.highload.services.ImageService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.impl.ImageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @CrossOrigin
    @PostMapping("/edit/{id}")
    public ResponseEntity edit(@RequestBody ProfileDto data, @PathVariable int id){
        if (profileService.editProfile(data, id) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save profile changes, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
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
    @GetMapping("/portfolio/{id}")
    public ResponseEntity getPortfolioById(@PathVariable int id, Pageable pageable){
        ProfileDto entity = profileService.findById(id);
        List<ImageDto> images = imageService.findAllProfileImages(id, pageable);
        // TODO Сонь ты лучше за pageable шаришь посмотри пж
        Page<ImageDto> pageImages = imageService.getPage(images, pageable);
        return ResponseEntity.ok(pageImages);
    }
}
