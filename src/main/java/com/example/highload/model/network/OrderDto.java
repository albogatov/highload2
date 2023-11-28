package com.example.highload.model.network;

import com.example.highload.model.enums.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto implements Serializable {
    int id;
    int userId;
    String userName;
    LocalDateTime created;
    @Min(0)
    int price;
    @NotBlank
    String description;
    @Size(max=10)
    List<TagDto> tags; // should be displayed all
    OrderStatus status;
}
