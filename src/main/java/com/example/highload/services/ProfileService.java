package com.example.highload.services;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.network.ProfileDto;
import com.example.highload.model.network.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProfileService {

    Profile saveProfileForUser(ProfileDto profileDto);
    Profile editProfile(ProfileDto data, int id);

    Profile findById(int id);

    Profile findByUserId(int userId);

    Page<Profile> findAllProfiles(Pageable pageable);
}
