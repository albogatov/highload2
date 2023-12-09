package com.example.user.services.impl;

import com.example.user.security.jwt.JwtUtil;
import com.example.user.services.AuthenticationService;
import com.example.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public String authProcess(String login, String password, String role) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
        return jwtUtil.generateToken(login, role);
    }
}
