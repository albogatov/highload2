package com.example.highload.services.impl;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.UserDto;
import com.example.highload.repos.UserRepository;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DataTransformer dataTransformer;

    @Override
    public UserDetailsService userDetailsService() {
        return login -> userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

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
