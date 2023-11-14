package com.example.highload.services;

import com.example.highload.model.inner.Response;
import com.example.highload.model.network.ResponseDto;

import java.util.List;

public interface ResponseService {

    ResponseDto saveResponse(ResponseDto data);

    List<ResponseDto> findAllForOrder(int orderId);

    List<ResponseDto> findAllForProfile(int profileId);

    ResponseDto findById(int id);
}
