package com.example.user.services;

import com.example.user.model.inner.User;
import com.example.user.model.inner.UserRequest;
import com.example.user.model.network.UserDto;
import com.example.user.model.network.UserRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;

public interface UserService {

    UserDetailsService userDetailsService();

    UserRequest addUserRequest(UserRequestDto userRequestDto);

    UserRequest findUserRequestByLoginElseNull(String login);

    User findByLoginElseNull(String login);

    User findById(int id);

    User saveUser(UserDto userDto);

    Page<UserRequest> getAllUserRequests(Pageable pageable);

    void deactivateById(int userId);

    Page<User> findAllExpired(LocalDateTime dateTimeLTDelete, Pageable pageable);

    void deleteAllExpired(LocalDateTime dateTimeLTDelete);

    UserRequest findUserRequestById(int userRequestId);

    User save(User user);

    void deleteUserRequest(UserRequest userRequest);

    void deleteById(Integer id);
}
