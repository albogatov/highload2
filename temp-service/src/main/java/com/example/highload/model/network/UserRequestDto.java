package com.example.highload.model.network;

import com.example.highload.model.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {

    private int id;
    @NotBlank
    @Size(min = 1, max = 50)
    private String login;
    @NotBlank
    private String password;
    private RoleType role;

}
