package com.example.highload.controllers;

import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;
    private final AuthenticationService authenticationService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto user) {
        if (user.getLogin() == null || user.getPassword() == null) {
            return new ResponseEntity<>("Absent login or password", HttpStatus.BAD_REQUEST);
        }
        JwtResponse response = JwtResponse.builder().token(authenticationService.authProcess(user.getLogin(), user.getPassword(),
                user.getRole().toString())).userId(userService.findByLoginElseNull(user.getLogin()).getId()).build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDto user) {

        if (userService.findByLoginElseNull(user.getLogin()) != null || userService.findUserRequestByLoginElseNull(user.getLogin()) != null) {
            return new ResponseEntity<>("This user already exists or is awaiting approval", HttpStatus.CONFLICT);
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userService.addUserRequest(user);
        return new ResponseEntity<>("User successfully registered", HttpStatus.OK);

    }

    @PostMapping("/profile/add/{userId}")
    public ResponseEntity addProfile(@Valid @RequestBody ProfileDto profile, @PathVariable int userId) {

        if (profileService.findByUserIdElseNull(userId) == null) {
            profileService.saveProfileForUser(profile, userId);
            return new ResponseEntity<>("Profile successfully added", HttpStatus.OK);
        }
        return new ResponseEntity<>("Profile already added", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable int id) {
        userService.deactivateById(id);
        return new ResponseEntity<>("Profile deactivated", HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions() {
        return ResponseEntity.badRequest().body("Request body validation failed!");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity handleServiceExceptions() {
        return ResponseEntity.badRequest().body("Wrong ids in path!");
    }

}
