package com.example.user.model.network;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewDto implements Serializable {

    int id;
    int profileId;
    String userName;
    @NotBlank
    String text;
}
