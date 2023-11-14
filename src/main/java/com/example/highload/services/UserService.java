package com.example.highload.services;

import com.example.highload.model.network.UserDto;

public interface UserService {

    UserDto findByLogin(String login);

    UserDto save(UserDto data);

}
