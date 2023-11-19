package com.example.highload.controllers;

import com.example.highload.model.inner.Tag;
import com.example.highload.model.network.TagDto;
import com.example.highload.services.TagService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/tag/")
@RequiredArgsConstructor
public class TagController {

    private TagService tagService;
    private final DataTransformer dataTransformer;

    @CrossOrigin
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity save(@RequestBody TagDto data){
        if(tagService.saveTag(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save tag, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    // todo: "findAll в виде бесконечной прокрутки без указания общего количества записей"
    public ResponseEntity getAll(){
        List<Tag> entityList = tagService.findAll();
        List<TagDto> dtoList = dataTransformer.tagListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }

}
