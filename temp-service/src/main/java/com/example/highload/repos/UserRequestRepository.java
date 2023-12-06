package com.example.highload.repos;

import com.example.highload.model.inner.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, Integer> {
    Optional<UserRequest> findByLogin(String login);
}
