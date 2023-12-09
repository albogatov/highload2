package com.example.user.services;

import com.example.user.model.inner.User;
import com.example.user.model.network.UserDto;

public interface AdminService {

    User approveUser(int userRequestId);

    User addUser(UserDto userDto);

    void deleteUser(int userId);

    void deleteLogicallyDeletedUsers(int daysToExpire);

}
