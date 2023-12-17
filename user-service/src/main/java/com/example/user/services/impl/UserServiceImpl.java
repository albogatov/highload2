package com.example.user.services.impl;

import com.example.user.model.inner.User;
import com.example.user.model.inner.UserRequest;
import com.example.user.model.network.UserDto;
import com.example.user.model.network.UserRequestDto;
import com.example.user.repos.UserRepository;
import com.example.user.repos.UserRequestRepository;
import com.example.user.services.UserService;
import com.example.user.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
        UserRequest userRequest = dataTransformer.userRequestFromDto(userRequestDto);
        return userRequestRepository.save(userRequest);
    }

    @Override
    public UserRequest findUserRequestByLoginElseNull(String login) {
        return userRequestRepository.findByLogin(login).orElse(null);
    }

    public User findByLoginElseNull(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    @Override
    public User saveUser(UserDto userDto) {
        User user = dataTransformer.userFromDto(userDto);
        user.setHashPassword(userDto.getPassword());
        return userRepository.save(user);
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
    public Page<User> findAllExpired(LocalDateTime dateTimeLTDelete, Pageable pageable) {
        return userRepository.findAllByIsActualFalseAndWhenDeletedTimeLessThan(dateTimeLTDelete, pageable).orElse(Page.empty());
    }

    @Override
    public void deleteAllExpired(LocalDateTime dateTimeLTDelete) {
        userRepository.deleteAllByIsActualFalseAndWhenDeletedTimeLessThan(dateTimeLTDelete);
    }

    @Override
    public UserRequest findUserRequestById(int userRequestId) {
        return userRequestRepository.findById(userRequestId).orElseThrow();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUserRequest(UserRequest userRequest) {
        userRequestRepository.deleteById(userRequest.getId());
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id).orElseThrow();
    }


}
