package com.example.user.services.impl;

import com.example.user.consumer.ImageRestConsumer;
import com.example.user.consumer.OrderRestConsumer;
import com.example.user.model.inner.ClientOrder;
import com.example.user.model.inner.Profile;
import com.example.user.model.inner.User;
import com.example.user.model.inner.UserRequest;
import com.example.user.model.network.UserDto;
import com.example.user.services.AdminService;
import com.example.user.services.ImageService;
import com.example.user.services.UserService;
import com.example.user.utils.DataTransformer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ImageRestConsumer imageRestConsumer;

    @Autowired
    private OrderRestConsumer orderRestConsumer;

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
                imageRestConsumer.deleteImages(profile.getId());
                orderRestConsumer.deleteOrders(profile.getId());
//                if (profile != null) {
//                    imageService.removeAllImagesForProfile(profile);
//                    if (profile.getImage() != null) {
//                        imageService.removeImageById(profile.getImage().getId());
//                    }
//
//                }
//
//                List<ClientOrder> orders = user.getOrders();
//                if (!orders.isEmpty())
//                    orders.forEach(imageService::removeAllImagesForOrder);
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
