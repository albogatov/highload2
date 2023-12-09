package com.example.highload.model.network;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseDto implements Serializable {

    int id;
    int userId;
    String userName;
    int orderId;
    String text;
    boolean isApproved;
}
