package com.example.highload.controllers;

import com.example.highload.exceptions.AppError;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api/app/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;
    private final AuthenticationService authenticationService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto user) {
        if (user.getLogin() == null || user.getPassword() == null) {
            return new ResponseEntity<>("Absent login or password", HttpStatus.BAD_REQUEST);
        }
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword()));
//        } catch (BadCredentialsException e) {
//            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Invalid login or password"), HttpStatus.UNAUTHORIZED);
//        }
        JwtResponse response = JwtResponse.builder().token(authenticationService.authProcess(user.getLogin(), user.getPassword(), user.getRole().toString())).build();
        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDto user) {
        try {
//            if (user.getLogin() == null || user.getPassword() == null || user.getLogin().trim().isEmpty()
//                    || user.getPassword().trim().isEmpty()) {
//                throw new IllegalArgumentException();
//            }

            if (userService.findByLogin(user.getLogin()) != null || userService.findUserRequestByLogin(user.getLogin()) != null) {
                return new ResponseEntity<>(new AppError(HttpStatus.CONFLICT.value(), "This user already exists or is awaiting approval"), HttpStatus.CONFLICT);
            }
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            userService.addUserRequest(user);
            return new ResponseEntity<>("User successfully registered", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid login or password", HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @PostMapping("/profile/add")
    public ResponseEntity addProfile(@Valid @RequestBody ProfileDto profile) {

        if (profileService.findByUserId(profile.getUserId()) == null) {
            profileService.saveProfileForUser(profile);
            return new ResponseEntity<>("Profile successfully added", HttpStatus.OK);
        }
        return new ResponseEntity<>("Profile already added", HttpStatus.BAD_REQUEST);
    }

    @CrossOrigin
    @PostMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable int id) {
        userService.deactivateById(id);
        return new ResponseEntity<>("Profile deactivated", HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(){
        return ResponseEntity.badRequest().body("Request body validation failed!");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity handleServiceExceptions(){
        return ResponseEntity.badRequest().body("Wrong ids in path!");
    }

}
