package com.example.highload.model.network;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewDto implements Serializable {

    int id;
    int profileId;
    String userName;
    String text;
}
