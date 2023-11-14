package com.example.highload.services;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;

import java.util.List;

public interface OrderService {

    OrderDto saveOrder(OrderDto orderDto);

    OrderDto updateOrder(OrderDto orderDto, int id);

    OrderDto getOrderById(int id);

    List<OrderDto> getUserOrders(int userId);

    List<ImageDto> getImagesForOrder(int id);


}
