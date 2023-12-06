package com.example.highload.services;

import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

    UserDetailsService userDetailsService();

    UserRequest addUserRequest(UserRequestDto userRequestDto);

    UserRequest findUserRequestByLogin(String login);

    User findByLogin(String login);

    User findById(int id);

    User saveUser(UserDto userDto);

    Page<UserRequest> getAllUserRequests(Pageable pageable);

    void deactivateById(int userId);

}
