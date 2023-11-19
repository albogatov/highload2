package com.example.highload.services;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.UserDto;

public interface UserService {

    User findByLogin(String login);

    User saveUser(UserDto userDto);

}
