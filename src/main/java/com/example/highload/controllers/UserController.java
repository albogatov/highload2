package com.example.highload.controllers;

import com.example.highload.exceptions.AppError;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
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

    private UserService userService;
    private ProfileService profileService;
    private final DataTransformer dataTransformer;
    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto user) {
        if (user.getLogin() == null || user.getPassword() == null) {
            return new ResponseEntity<>("Absent login or password", HttpStatus.BAD_REQUEST);
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Неправильный логин или пароль"), HttpStatus.UNAUTHORIZED);
        }
        JwtResponse response = JwtResponse.builder().token(authenticationService.Auth(user.getLogin(), user.getPassword())).build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDto user) {
        try {
            if (user.getLogin() == null || user.getPassword() == null || user.getLogin().trim().equals("")
                    || user.getPassword().trim().equals("")) {
                throw new IllegalArgumentException();
            }

            if (userService.findByLogin(user.getLogin()) != null || userService.findUserRequestByLogin(user.getLogin()) != null) {
                return new ResponseEntity<>(new AppError(HttpStatus.CONFLICT.value(), "This user already exists or is awaiting approval"), HttpStatus.CONFLICT);
            }
            //TODO: SECURITY
            user.setPassword(passwordEncoder.encode(user.getPassword()));
//            profileService.saveProfileForUser(user);
            // todo решить, каким образом передавать профиль при регистрации
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
        } else {
            return new ResponseEntity<>("Invalid login or password", HttpStatus.BAD_REQUEST);
        }
    }

}
