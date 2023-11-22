package com.example.highload.model.network;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtRequest implements Serializable {
    private String login;
    private String password;
}
