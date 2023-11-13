package com.example.highload.repos;

import com.example.highload.model.inner.Response;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Integer> {

    List<Response> findAllByUser_Id(Integer id, Pageable pageable);
    List<Response> findAllByOrder_Id(Integer id, Pageable pageable);

}
