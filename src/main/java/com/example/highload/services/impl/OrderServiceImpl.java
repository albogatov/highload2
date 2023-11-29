package com.example.highload.services.impl;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.Tag;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.repos.TagRepository;
import com.example.highload.services.OrderService;
import com.example.highload.utils.DataTransformer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TagRepository tagRepository;
    private final DataTransformer dataTransformer;

    @Override
    public ClientOrder saveOrder(OrderDto orderDto) {
        if (orderDto.getTags().size() > 10) return null;
        return orderRepository.save(dataTransformer.orderFromDto(orderDto));
    }

    @Override
    public ClientOrder updateOrder(OrderDto orderDto, int id) {
        ClientOrder order = orderRepository.findById(id).orElseThrow();
        order.setPrice(orderDto.getPrice());
        order.setDescription(orderDto.getDescription());
        order.setStatus(orderDto.getStatus());
        // TODO TAGS ADD/DELETE
        orderRepository.save(order);
        return order;
    }

    @Override
    public ClientOrder getOrderById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Page<ClientOrder> getUserOrders(int userId, Pageable pageable) {
        return orderRepository.findAllByUser_Id(userId, pageable);
    }

    @Override
    public Page<ClientOrder> getUserOpenOrders(int userId, Pageable pageable) {
        return orderRepository.findAllByUser_IdAndStatus(userId, OrderStatus.OPEN, pageable);
    }

    @Override
    public Page<ClientOrder> getOrdersByTags(List<Integer> tagIds, Pageable pageable) {
        return orderRepository.findAllByMultipleTagsIds(tagIds, pageable);
    }

    @Override
    public Page<ClientOrder> getOpenOrdersByTags(List<Integer> tagIds, Pageable pageable) {
        return orderRepository.findAllByMultipleTagsIdsAndStatus(tagIds, OrderStatus.OPEN.toString(), pageable);
    }

    @Override
    public Page<ClientOrder> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public ClientOrder addTagsToOrder(List<Integer> tagIds, int orderId) {
        ClientOrder order = orderRepository.findById(orderId).orElseThrow();
        List<Integer> oldTagIds = order.getTags().stream().map(Tag::getId).toList();
        List<Integer> tagIdsToAdd = tagIds.stream().filter(i -> !oldTagIds.contains(i)).toList();
        if (tagIdsToAdd.size() + oldTagIds.size() <= 10) {
            List<Tag> tagsToAdd = new ArrayList<>();
            for (Integer tagIdToAdd : tagIdsToAdd) {
                Tag tag = tagRepository.findById(tagIdToAdd).orElseThrow();
                tagsToAdd.add(tag);
            }
            order.getTags().addAll(tagsToAdd);
            orderRepository.save(order);
            return order;
        }
        return null;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public ClientOrder deleteTagsFromOrder(List<Integer> tagIds, int orderId) {
        ClientOrder order = orderRepository.findById(orderId).orElseThrow();
        List<Integer> oldTagIds = order.getTags().stream().map(Tag::getId).toList();
        for (Integer tagIdToDelete : tagIds) {
            if (!oldTagIds.contains(tagIdToDelete)) {
                return null;
            }
        }
        List<Tag> newTagList = order.getTags().stream().filter(tag -> !tagIds.contains(tag.getId())).toList();
        order.setTags(newTagList);
        orderRepository.save(order);
        return order;

    }
}
