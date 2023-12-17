package com.example.highload.services.impl;

import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.inner.User;
import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.UserDto;
import com.example.highload.services.AdminService;
import com.example.highload.services.ImageService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final ImageService imageService;
    private final DataTransformer dataTransformer;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {Exception.class})
    public void deleteLogicallyDeletedUsers(int daysToExpire) {
        LocalDateTime dateTimeLTDelete = LocalDateTime.now().minusDays(daysToExpire);
        Page<User> usersToDelete;
        Pageable pageable;
        int i = 0;
        do {
            pageable = PageRequest.of(i, 50);
            usersToDelete = userService.findAllExpired(dateTimeLTDelete, pageable);
            for (User user :
                    usersToDelete.getContent()) {

                Profile profile = user.getProfile();
                if (profile != null) {
                    imageService.removeAllImagesForProfile(profile);
                    if (profile.getImage() != null) {
                        imageService.removeImageById(profile.getImage().getId());
                    }

                }

                List<ClientOrder> orders = user.getOrders();
                if (!orders.isEmpty())
                    orders.forEach(imageService::removeAllImagesForOrder);
            }
            i++;
        } while (usersToDelete.getContent().size() == 50);

        userService.deleteAllExpired(dateTimeLTDelete);

    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public User approveUser(int userRequestId) {
        UserRequest userRequest = userService.findUserRequestById(userRequestId);
        User user = new User();
        user.setLogin(userRequest.getLogin());
        user.setHashPassword(userRequest.getHashPassword());
        user.setRole(userRequest.getRole());
        user.setIsActual(true);
        user = userService.save(user);
        userService.deleteUserRequest(userRequest);
        return user;
    }

    @Override
    public User addUser(UserDto userDto) {
        User user = dataTransformer.userFromDto(userDto);
        user.setHashPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        return userService.save(user);
    }

    @Override
    public void deleteUser(int userId) {
        User user = userService.findById(userId);
        userService.deleteById(user.getId());
    }
}
