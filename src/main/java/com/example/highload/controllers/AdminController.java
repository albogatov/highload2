package com.example.highload.controllers;

import com.example.highload.exceptions.AppError;
import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.JwtResponse;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.repos.UserRequestRepo;
import com.example.highload.security.jwt.JwtUtil;
import com.example.highload.services.AdminService;
import com.example.highload.services.AuthenticationService;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/app/admin/")
@RequiredArgsConstructor
public class AdminController {

    private UserService userService;
    private AdminService adminService;

    private ProfileService profileService;
    private final DataTransformer dataTransformer;

    @PostMapping("/user/approve/{id}")
    @CrossOrigin
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity approveUserRequest(@PathVariable int id) {
        UserRequest userRequest = adminService.findUserRequest(id);
        adminService.approveUser(dataTransformer.userRequestToDto(userRequest));
        return ResponseEntity.ok("User approved");
    }

    @PostMapping("/user/delete/{id}")
    @CrossOrigin
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity deleteUser(@PathVariable int id) {
        User user = userService.findById(id);
        adminService.deleteUser(dataTransformer.userToDto(user));
        return ResponseEntity.ok("User delete");
    }

    @PostMapping("/user/add/")
    @CrossOrigin
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity addUser(@RequestBody UserDto user) {
        adminService.addUser(user);
        return ResponseEntity.ok("User delete");
    }

}
