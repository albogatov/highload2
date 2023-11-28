package com.example.highload.services.impl;

import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.repos.UserRepository;
import com.example.highload.repos.UserRequestRepository;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRequestRepository userRequestRepository;
    private final DataTransformer dataTransformer;

    @Override
    public UserDetailsService userDetailsService() {
        return login -> userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserRequest addUserRequest(UserRequestDto userRequestDto) {
        return userRequestRepository.save(dataTransformer.userRequestFromDto(userRequestDto));
    }

    @Override
    public UserRequest findUserRequestByLogin(String login) {
        return userRequestRepository.findByLogin(login).orElse(null);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    @Override
    public User saveUser(UserDto userDto) {
        return userRepository.save(dataTransformer.userFromDto(userDto));
    }

    @Override
    public Page<UserRequest> getAllUserRequests(Pageable pageable) {
        return userRequestRepository.findAll(pageable);
    }

    @Override
    public void deactivateById(int userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setIsActual(false);
        user.setWhenDeletedTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }


}
