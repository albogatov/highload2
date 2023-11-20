package com.example.highload.controllers;

import com.example.highload.model.inner.User;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/app/user/")
@RequiredArgsConstructor
public class UserController {

    private UserService userService;
    private ProfileService profileService;
    private final DataTransformer dataTransformer;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody UserDto user) {
        if (user.getLogin() == null || user.getPassword() == null) {
            return new ResponseEntity<>("Absent login or password", HttpStatus.BAD_REQUEST);
        }
        try {
            // TODO: SECURITY
            String login = user.getLogin();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, user.getPassword()));
            String token = jwtUtil.resolveToken(login);
            User userEntity = userService.findByLogin(user.getLogin());
            UserDto userDto = dataTransformer.userToDto(userEntity);
            // TODO Remove ResponseMessageEntity
            return new ResponseEntity<>(new ResponseMessageEntity(token, userDto.getRole(), HttpStatus.OK));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Wrong login or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody UserDto user) {
        try {
            if (user.getLogin() == null || user.getPassword() == null || user.getLogin().trim().equals("")
                    || user.getPassword().trim().equals("")) {
                throw new IllegalArgumentException();
            }

            if (userService.findByLogin(user.getLogin()) != null) {
                return new ResponseEntity<>("User already registered", HttpStatus.CONFLICT);
            }
            //TODO: SECURITY
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
//            profileService.saveProfileForUser(user);
            // todo решить, каким образом передавать профиль при регистрации
            userService.saveUser(user);
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
