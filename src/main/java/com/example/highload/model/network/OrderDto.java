package com.example.highload.model.network;

import com.example.highload.model.enums.OrderStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto implements Serializable {
    int id;
    int userId;
    String userName;
    LocalDateTime created;
    int price;
    String description;
    List<TagDto> tags; // should be displayed all
    OrderStatus status;
}
