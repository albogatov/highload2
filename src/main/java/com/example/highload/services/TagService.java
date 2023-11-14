package com.example.highload.services;

import com.example.highload.model.network.TagDto;

import java.util.List;

public interface TagService {

    TagDto saveTag(TagDto data);

    List<TagDto> findAll();

}
