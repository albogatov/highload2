package com.example.highload.controllers;

import com.example.highload.model.inner.Response;
import com.example.highload.model.network.ResponseDto;
import com.example.highload.model.network.ReviewDto;
import com.example.highload.services.ResponseService;
import com.example.highload.utils.DataTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/response/")
@RequiredArgsConstructor
public class ResponseController {

    private ResponseService responseService;
    private final DataTransformer dataTransformer;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody ResponseDto data){
        if(responseService.saveResponse(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save response, check data");
    }

    @CrossOrigin
    @GetMapping("/all/{orderId}")
    @PreAuthorize("hasAnyAuthority('CLIENT')")
    // todo: "findAll в виде бесконечной прокрутки без указания общего количества записей"
    public ResponseEntity getAllByOrder(@PathVariable int orderId){
        List<Response> entityList = responseService.findAllForOrder(orderId);
        List<ResponseDto> dtoList = dataTransformer.responseListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }

    @CrossOrigin
    @GetMapping("/all/{userId}")
    @PreAuthorize("hasAnyAuthority('ARTIST')")
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getAllByProfile(@PathVariable int userId){
        List<Response> entityList = responseService.findAllForUser(userId);
        List<ResponseDto> dtoList = dataTransformer.responseListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }

    @CrossOrigin
    @GetMapping("/single/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@PathVariable int id){
        Response entity = responseService.findById(id);
        return ResponseEntity.ok(dataTransformer.responseToDto(entity));
    }


}
