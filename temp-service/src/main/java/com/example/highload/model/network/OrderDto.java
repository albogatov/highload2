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
    private int id;
    private int userId;
    private String userName;
    private LocalDateTime created;
    @Min(0)
    private int price;
    @NotBlank
    private String description;
    @Size(max=10)
    private List<TagDto> tags;
    private OrderStatus status;
}
