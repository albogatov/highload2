package com.example.highload.controllers;

import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Tag;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.TagRepository;
import com.example.highload.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/tag/")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagService tagService;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(tagRepository.save(tagService.prepareEntity(data)) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save tag, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
        List<Tag> entityList = tagRepository.findAll();
        return ResponseEntity.ok(entityList);
    }

}
