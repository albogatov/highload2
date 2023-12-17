package com.example.order.services;

import com.example.order.model.inner.Tag;
import com.example.order.model.network.TagDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TagService {

    Tag saveTag(TagDto tagDto);

    Page<Tag> findAll(Pageable pageable);

    void removeTagFromOrder(int tagId, int orderId);

    Tag findById(Integer tagIdToAdd);
}
