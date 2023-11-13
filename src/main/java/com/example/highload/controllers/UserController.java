package com.example.highload.controllers;

import com.example.highload.model.inner.Profile;
import com.example.highload.model.inner.User;
import com.example.highload.model.network.UserDto;
import com.example.highload.repos.ProfileRepository;
import com.example.highload.repos.UserRepository;
import com.example.highload.services.ProfileService;
import com.example.highload.services.UserService;
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
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody UserDto user){
        if (user.getLogin() == null || user.getPassword() == null) {
            logger.error("Absent login or password");
            return new ResponseEntity<>("Absent login or password", HttpStatus.BAD_REQUEST);
        }
        try {
            // TODO: SECURITY
            String login = user.getLogin();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, user.getPassword()));
            String token = jwtUtil.resolveToken(login);
            Optional<User> userEntity = userRepository.findByLogin(user.getLogin());
            return new ResponseEntity<>(new ResponseMessageEntity(token, user.getRole(), HttpStatus.OK));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Wrong login or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody UserDto user){
        logger.debug("registering user");
        try {
            logger.debug(user.toString());
            if (user.getLogin() == null || user.getPassword() == null || user.getLogin().trim().equals("")
                    || user.getPassword().trim().equals("")) {
                logger.error("Absent login or password");
                throw new IllegalArgumentException();
            }

            if (userService.find(user.getLogin()) != null) {
                logger.error("Already registered" + userService.find(user.getLogin()));
                return new ResponseEntity<>("User already registered", HttpStatus.CONFLICT);
            }
            //TODO: SECURITY
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            User userEntity = userRepository.save(userRepository.prepareEntity(user));
            Profile userProfile = profileService.prepareEntity();
            userProfile.setUser(userEntity);
            profileRepository.save(userProfile);
            userRepository.save(userService.prepareEntity(user));
            return new ResponseEntity<>("User successfully registered", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid register");
            return new ResponseEntity<>("Invalid login or password", HttpStatus.BAD_REQUEST);
        }
    }

}
