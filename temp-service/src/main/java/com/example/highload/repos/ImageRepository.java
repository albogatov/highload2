package com.example.highload.repos;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.ClientOrder;
import com.example.highload.model.inner.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    Optional<Page<Image>> findAllByImageObject_Order_Id(Integer orderId, Pageable pageable);
    Optional<Page<Image>> findAllByImageObject_Profile_Id(Integer profileId, Pageable pageable);

    void deleteAllByImageObject_Order(ClientOrder order);
    void deleteAllByImageObject_Profile(Profile profile);

}