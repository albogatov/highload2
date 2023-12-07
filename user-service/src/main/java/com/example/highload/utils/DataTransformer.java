package com.example.highload.utils;

import com.example.highload.model.enums.RoleType;
import com.example.highload.model.inner.*;
import com.example.highload.model.network.*;
import com.example.highload.repos.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("userService.dataTransformer")
@Data
@AllArgsConstructor
public class DataTransformer {


    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    /* users */

    public User userFromDto(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setLogin(userDto.getLogin());
        user.setHashPassword(userDto.getPassword());
        RoleType roleName = userDto.getRole();
        Role role = roleRepository.findByName(roleName).orElseThrow();
        user.setRole(role);
        user.setIsActual(true);
        return user;
    }


    /* user requests */

    public UserRequestDto userRequestToDto(UserRequest userRequest) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setId(userRequest.getId());
        userRequestDto.setLogin(userRequest.getLogin());
        userRequestDto.setRole(userRequest.getRole().getName());
        return userRequestDto;
    }

    public UserRequest userRequestFromDto(UserRequestDto userRequestDto) {
        UserRequest userRequest = new UserRequest();
        userRequest.setId(userRequestDto.getId());
        userRequest.setLogin(userRequestDto.getLogin());
        userRequest.setHashPassword(userRequestDto.getPassword());
        Role role = roleRepository.findByName(userRequestDto.getRole()).orElseThrow();
        userRequest.setRole(role);
        return userRequest;
    }

    public List<UserRequestDto> userRequestListToDto(List<UserRequest> entities) {
        return entities.stream().map(this::userRequestToDto).toList();
    }


    /* profiles */

    public ProfileDto profileToDto(Profile profile) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setId(profile.getId());
        profileDto.setName(profile.getName());
        profileDto.setAbout(profile.getAbout());
        if (profile.getImage() != null) {
            profileDto.setImage(imageToDto(profile.getImage()));
        }
        profileDto.setMail(profile.getMail());
        profileDto.setEducation(profile.getEducation());
        profileDto.setExperience(profile.getExperience());
        profileDto.setUserId(profile.getUser().getId());
        return profileDto;
    }

    public Profile profileFromDto(ProfileDto profileDto) {
        Profile profile = new Profile();
        profile.setId(profileDto.getId());
        profile.setName(profileDto.getName());
        profile.setAbout(profileDto.getAbout());
        if (profileDto.getImage() != null) {
            Image image = imageRepository.findById(profileDto.getImage().getId()).orElseThrow();
            profile.setImage(image);
        }
        profile.setMail(profileDto.getMail());
        profile.setEducation(profileDto.getEducation());
        profile.setExperience(profileDto.getExperience());
        User user = userRepository.findById(profileDto.getUserId()).orElseThrow();
        profile.setUser(user);
        return profile;
    }

    public List<ProfileDto> profileListToDto(List<Profile> entities) {
        return entities.stream().map(this::profileToDto).toList();
    }
}
