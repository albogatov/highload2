package com.example.order.services.impl;

import com.example.order.model.inner.Response;
import com.example.order.model.network.ResponseDto;
import com.example.order.repos.ResponseRepository;
import com.example.order.services.ResponseService;
import com.example.order.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseServiceImpl implements ResponseService {

    private final ResponseRepository responseRepository;
    private final DataTransformer dataTransformer;

    @Override
    public Response saveResponse(ResponseDto responseDto) {
        return responseRepository.save(dataTransformer.responseFromDto(responseDto));
    }

    @Override
    public Page<Response> findAllForOrder(int orderId, Pageable pageable) {
        return responseRepository.findAllByOrder_Id(orderId, pageable).orElse(Page.empty());
    }

    @Override
    public Page<Response> findAllForUser(int userId, Pageable pageable) {
        return responseRepository.findAllByUser_Id(userId, pageable).orElse(Page.empty());
    }

    @Override
    public Response findById(int id) {
        return responseRepository.findById(id).orElseThrow();
    }
}
