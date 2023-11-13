package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Profile;
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
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;

    @CrossOrigin
    @PostMapping("/edit")
    public ResponseEntity edit(@RequestBody ProfileDto data){
        if(profileRepository.save(profileService.prepareEntity(data)) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save profile changes, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
        List<Profile> entityList = profileRepository.findAll();
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single")
    public ResponseEntity getById(@RequestBody IdDto data){
        Profile entity = profileRepository.findById(data.getId()).get();
        return ResponseEntity.ok(profileService.prepareDto(entity));
    }

    @CrossOrigin
    @GetMapping("/portfolio")
    public ResponseEntity getPortfolioById(@RequestBody IdDto data, Pageable pageable){
        Profile entity = profileRepository.findById(data.getId()).get();
        List<Image> images = imageRepository.findAllByImageObject_Profile(entity, pageable);
        Page<Image> pageImages = imageService.getPage(images, pageable);
        return ResponseEntity.ok(pageImages);
    }
}
