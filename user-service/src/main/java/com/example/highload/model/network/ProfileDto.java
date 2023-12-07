package com.example.highload.model.network;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProfileDto implements Serializable {
    // TODO: DTO should be commons or in dependency
    int id;
    int userId;
    ImageDto image;
    @NotBlank
    String name;
    String experience;
    String education;
    String about;
    @NotBlank
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{1,4}$")
    String mail;
}
