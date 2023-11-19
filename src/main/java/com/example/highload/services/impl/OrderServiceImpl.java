package com.example.highload.services.impl;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.services.OrderService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DataTransformer dataTransformer;

    @Override
    public Order saveOrder(OrderDto orderDto) {
        return orderRepository.save(dataTransformer.orderFromDto(orderDto));
    }

    @Override
    public Order updateOrder(OrderDto orderDto, int id) {
        // TODO UPDATES IN REPOS
    }

    @Override
    public Order getOrderById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Order> getUserOrders(int userId, Pageable pageable) {
        return orderRepository.findAllByUser_Id(userId, pageable);
    }

    @Override
    public Page<Order> getUserOpenOrders(int userId, Pageable pageable) {
        return orderRepository.findAllByUser_IdAndStatus(userId, OrderStatus.OPEN, pageable);
    }

    @Override
    public Page<Order> getOrdersByTags(List<Integer> tagIds, Pageable pageable) {
        return orderRepository.findAllByMultipleTagsIds(tagIds, pageable);
    }

    @Override
    public Page<Order> getOpenOrdersByTags(List<Integer> tagIds, Pageable pageable) {
        return orderRepository.findAllByMultipleTagsIdsAndStatus(tagIds, OrderStatus.OPEN.toString(), pageable);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
