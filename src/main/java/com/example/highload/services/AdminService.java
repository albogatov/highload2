package com.example.highload.services;

import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;

public interface AdminService {

    UserRequest findUserRequest(int id);
    User approveUser(UserRequestDto userRequestDto);

    User addUser(UserDto userDto);

    void deleteUser(UserDto userDto);

}
