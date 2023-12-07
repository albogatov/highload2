package com.example.highload.services;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.UserDto;

public interface AdminService {

    User approveUser(int userRequestId);

    User addUser(UserDto userDto);

    void deleteUser(int userId);

    void deleteLogicallyDeletedUsers(int daysToExpire);

}
