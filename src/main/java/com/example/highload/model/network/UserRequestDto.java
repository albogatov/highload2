package com.example.highload.model.network;

import com.example.highload.model.enums.RoleType;
import lombok.Data;

@Data
public class UserRequestDto {

    int id;
    String login;
    String password;

}
