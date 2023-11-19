package com.example.highload.model.network;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProfileDto implements Serializable {

    int id;
    int userId;
    ImageDto image;
    String name;
    String experience;
    String education;
    String about;
    String mail;
}
