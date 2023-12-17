package com.example.user.services;

import com.example.user.model.inner.Image;
import com.example.user.model.inner.Profile;
import com.example.user.model.network.ProfileDto;
import com.example.user.model.network.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProfileService {

    Profile saveProfileForUser(ProfileDto profileDto, int userId);
    Profile editProfile(ProfileDto profileDto, int id);

    Profile findById(int id);

    Profile findByUserIdElseNull(int userId);

    Page<Profile> findAllProfiles(Pageable pageable);

    // TODO ???
    Image setNewMainImage(int profileId, Image newImage);
}
