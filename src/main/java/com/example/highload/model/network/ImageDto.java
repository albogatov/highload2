package com.example.highload.model.network;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class ImageDto implements Serializable {

    int id;
    @NotBlank
    String url;

}
