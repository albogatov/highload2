package com.example.highload.controllers;

import com.example.highload.exceptions.AppError;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/app/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;
    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto user) {
        if (user.getLogin() == null || user.getPassword() == null) {
            return new ResponseEntity<>("Absent login or password", HttpStatus.BAD_REQUEST);
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Invalid login or password"), HttpStatus.UNAUTHORIZED);
        }
        JwtResponse response = JwtResponse.builder().token(authenticationService.Auth(user.getLogin(), user.getPassword())).build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDto user) {
        try {
            if (user.getLogin() == null || user.getPassword() == null || user.getLogin().trim().isEmpty()
                    || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException();
            }

            if (userService.findByLogin(user.getLogin()) != null || userService.findUserRequestByLogin(user.getLogin()) != null) {
                return new ResponseEntity<>(new AppError(HttpStatus.CONFLICT.value(), "This user already exists or is awaiting approval"), HttpStatus.CONFLICT);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.addUserRequest(user);
            return new ResponseEntity<>("User successfully registered", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid login or password", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/profile/add")
    public ResponseEntity addProfile(@RequestBody ProfileDto profile) {

        if (profileService.findByUserId(profile.getUserId()) == null) {
            profileService.saveProfileForUser(profile);
            return new ResponseEntity<>("Profile successfully added", HttpStatus.OK);
        }
        return new ResponseEntity<>("Profile already added", HttpStatus.BAD_REQUEST);
    }

    /* TODO let user logically delete his account (not from db but change status) */

}
