package com.example.highload.services.impl;

import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Profile;
import com.example.highload.model.inner.User;
import com.example.highload.repos.ImageRepository;
import com.example.highload.repos.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public void deleteLogicallyDeletedUsers(Integer daysToExpire) {
        LocalDateTime dateTimeLTDelete = LocalDateTime.now().minusDays(daysToExpire);
        List<User> usersToDelete;
        Pageable pageable;
        int i = 0;
        do {
            pageable = PageRequest.of(i, 50);
            usersToDelete = userRepository.findAllByIsActualFalseAndWhenDeletedTimeLessThan(dateTimeLTDelete, pageable);
            for (User user :
                    usersToDelete) {

                Profile profile = user.getProfile();
                imageRepository.deleteAllByImageObject_Profile(profile);
                imageRepository.deleteById(profile.getImage().getId());

                List<Order> orders = user.getOrders();
                orders.forEach(imageRepository::deleteAllByImageObject_Order);
            }
            i++;
        } while (usersToDelete.size() == 50);

        userRepository.deleteAllByIsActualFalseAndWhenDeletedTimeLessThan(dateTimeLTDelete);

    }

}
