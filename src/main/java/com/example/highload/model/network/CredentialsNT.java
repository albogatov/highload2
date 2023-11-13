package com.example.highload.model.network;

import lombok.Data;

import java.io.Serializable;

@Data
public class CredentialsNT implements Serializable {

    String login;
    String token;

}
