package com.example.user.repos;

import com.example.user.model.inner.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByLogin(String login);
    Page<User> findAllByIsActualFalseAndWhenDeletedTimeLessThan(LocalDateTime timeLTDelete, Pageable pageable);

    void deleteAllByIsActualFalseAndWhenDeletedTimeLessThan(LocalDateTime timeLTDelete);

}
