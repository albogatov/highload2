package com.example.highload.services;

import com.example.highload.model.inner.Response;
import com.example.highload.model.network.ResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResponseService {

    Response saveResponse(ResponseDto responseDto);

    Page<Response> findAllForOrder(int orderId, Pageable pageable);

    Page<Response> findAllForProfile(int profileId, Pageable pageable);

    Response findById(int id);
}
