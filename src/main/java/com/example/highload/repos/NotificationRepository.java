package com.example.highload.repos;

import com.example.highload.model.inner.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByReceiverProfile_Id(Integer id, Pageable pageable);
    List<Notification> findAllBySenderProfile_Id(Integer id, Pageable pageable);
    List<Notification> findAllByIsReadFalseAndReceiverProfile_Id(Integer receiverId, Pageable pageable);

}
