package com.example.highload.services.impl;

import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.Tag;
import com.example.highload.model.network.TagDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.repos.TagRepository;
import com.example.highload.services.TagService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final OrderRepository orderRepository;
    private final TagRepository tagRepository;
    private final DataTransformer dataTransformer;

    @Override
    public Tag saveTag(TagDto tagDto) {
        return tagRepository.save(dataTransformer.tagFromDto(tagDto));
    }

    @Override
    public Page<Tag> findAll(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    @Override
    public void removeTagFromOrder(int tagId, int orderId) {
        Tag tagToRemove = tagRepository.findById(tagId).orElseThrow();
        ClientOrder order = orderRepository.findById(orderId).orElseThrow();
        order.setTags(new ArrayList<Tag>(order.getTags().stream().filter(tag -> tag.getId()!=tagId).toList()));
        orderRepository.save(order);
    }
}
