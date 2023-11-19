package com.example.highload.model.network;

import lombok.Data;

import java.io.Serializable;

@Data
public class TagDto implements Serializable {

    int id;
    String name;
}
