package com.example.user.services;

import com.example.user.model.inner.User;
import com.example.user.model.inner.UserRequest;
import com.example.user.model.network.UserDto;
import com.example.user.model.network.UserRequestDto;
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
