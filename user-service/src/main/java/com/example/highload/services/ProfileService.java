package com.example.highload.services;

import com.example.highload.model.inner.Profile;
import com.example.highload.model.network.ProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileService {

    Profile saveProfileForUser(ProfileDto profileDto, int userId);
    Profile editProfile(ProfileDto profileDto, int id);

    Profile findById(int id);

    Profile findByUserId(int userId);

    Page<Profile> findAllProfiles(Pageable pageable);
}
