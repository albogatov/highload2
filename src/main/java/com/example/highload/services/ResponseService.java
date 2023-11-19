package com.example.highload.services;

import com.example.highload.model.inner.Response;
import com.example.highload.model.network.ResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResponseService {

    ResponseDto saveResponse(ResponseDto data);

    Page<ResponseDto> findAllForOrder(int orderId, Pageable pageable);

    Page<ResponseDto> findAllForProfile(int profileId, Pageable pageable);

    ResponseDto findById(int id);
}
