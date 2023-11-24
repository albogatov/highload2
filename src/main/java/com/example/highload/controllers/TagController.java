package com.example.highload.controllers;

import com.example.highload.model.inner.Tag;
import com.example.highload.model.network.TagDto;
import com.example.highload.services.TagService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/tag/")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final PaginationHeadersCreator paginationHeadersCreator;
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
    @GetMapping("/all/{page}")
    public ResponseEntity getAll(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<Tag> entityList = tagService.findAll(pageable);
        List<TagDto> dtoList = dataTransformer.tagListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.endlessSwipeHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }

    @CrossOrigin
    @PostMapping("/remove/{orderId}/{tagId}")
    public ResponseEntity removeTagFromOrder(@PathVariable int orderId, @PathVariable int tagId) {
        tagService.removeTagFromOrder(tagId, orderId);
        return ResponseEntity.ok("");
    }

}
