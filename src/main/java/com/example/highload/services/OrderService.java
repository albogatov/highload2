package com.example.highload.services;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderDto saveOrder(OrderDto orderDto);

    OrderDto updateOrder(OrderDto orderDto, int id);

    OrderDto getOrderById(int id);

    Page<OrderDto> getUserOrders(int userId, Pageable pageable);

    Page<OrderDto> getUserOpenOrders(int userId, Pageable pageable);

    Page<ImageDto> getImagesForOrder(int id, Pageable pageable);


}
