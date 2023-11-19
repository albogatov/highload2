package com.example.highload.services;

import com.example.highload.model.network.TagDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {

    TagDto saveTag(TagDto data);

    Page<TagDto> findAll(Pageable pageable);

}
