package com.example.highload.model.network;

import com.example.highload.model.enums.RoleType;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {

    int id;
    String login;
    String password;
    RoleType role;

}
