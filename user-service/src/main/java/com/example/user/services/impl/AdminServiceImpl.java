package com.example.user.services.impl;

import com.example.user.consumer.ImageRestConsumer;
import com.example.user.consumer.OrderRestConsumer;
import com.example.user.model.inner.Profile;
import com.example.user.model.inner.User;
import com.example.user.model.inner.UserRequest;
import com.example.user.model.network.UserDto;
import com.example.user.repos.UserRepository;
import com.example.user.repos.UserRequestRepository;
import com.example.user.services.AdminService;
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

    // TODO: Separate admin and image processing or think about microservice structure a bit more
    private final UserRepository userRepository;
    private final UserRequestRepository userRequestRepository;

    @Autowired
    private ImageRestConsumer imageRestConsumer;

    @Autowired
    private OrderRestConsumer orderRestConsumer;
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
            usersToDelete = userRepository.findAllByIsActualFalseAndWhenDeletedTimeLessThan(dateTimeLTDelete, pageable);
            for (User user :
                    usersToDelete.getContent()) {

                Profile profile = user.getProfile();
                // TODO: Deletion of images and orders to be done by image and order services
                imageRestConsumer.deleteImages(profile.getId());
                orderRestConsumer.deleteOrders(profile.getId());
//                if (profile != null) {
//                    imageRepository.deleteAllByImageObject_Profile(profile);
//                    imageRepository.deleteById(profile.getImage().getId());
//                }
//
//                List<ClientOrder> orders = user.getOrders();
//                if (orders.size() > 0)
//                    orders.forEach(imageRepository::deleteAllByImageObject_Order);
            }
            i++;
        } while (usersToDelete.getContent().size() == 50);

        userRepository.deleteAllByIsActualFalseAndWhenDeletedTimeLessThan(dateTimeLTDelete);

    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = {NoSuchElementException.class, Exception.class})
    public User approveUser(int userRequestId) {
        UserRequest userRequest = userRequestRepository.findById(userRequestId).orElseThrow();
        User user = new User();
        user.setLogin(userRequest.getLogin());
        user.setHashPassword(userRequest.getHashPassword());
        user.setRole(userRequest.getRole());
        user.setIsActual(true);
        user = userRepository.save(user);
        userRequestRepository.delete(userRequest);
        return user;
    }

    @Override
    public User addUser(UserDto userDto) {
        User user = dataTransformer.userFromDto(userDto);
        user.setHashPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(int userId) {
        User user = userRepository.findById(userId).orElseThrow();
        userRepository.deleteById(user.getId());
    }
}
