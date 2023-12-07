package com.example.highload.repos;

import com.example.highload.model.inner.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    // TODO: Repositories must be switched to reactive programming (return Mono or Flux, extend Reactive)
    Optional<Profile> findByUser_Id(Integer id);

}
