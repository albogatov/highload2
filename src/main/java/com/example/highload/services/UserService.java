package com.example.highload.services;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

    UserDetailsService userDetailsService();

    User findByLogin(String login);

    User saveUser(UserDto userDto);

}
