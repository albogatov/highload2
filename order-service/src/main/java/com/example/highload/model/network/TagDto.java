package com.example.highload.model.network;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class TagDto implements Serializable {

    int id;
    @NotBlank
    String name;
}
