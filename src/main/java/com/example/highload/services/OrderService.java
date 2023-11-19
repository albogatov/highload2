package com.example.highload.services;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    Order saveOrder(OrderDto orderDto);

    Order updateOrder(OrderDto orderDto, int id);

    Order getOrderById(int id);

    Page<Order> getUserOrders(int userId, Pageable pageable);

    Page<Order> getUserOpenOrders(int userId, Pageable pageable);

    Page<Order> getOrdersByTags(List<Integer> tagIds, Pageable pageable);

    Page<Order> getOpenOrdersByTags(List<Integer> tagIds, Pageable pageable);

    Page<Order> getAllOrders(Pageable pageable);


}
