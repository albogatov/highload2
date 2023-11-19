package com.example.highload.services.impl;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.UserDto;
import com.example.highload.repos.UserRepository;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DataTransformer dataTransformer;

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    @Override
    public User saveUser(UserDto userDto) {
        return userRepository.save(dataTransformer.userFromDto(userDto));
    }

    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }



}
