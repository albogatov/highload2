package com.example.highload.controllers;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Response;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.model.network.ResponseDto;
import com.example.highload.repos.ResponseRepository;
import com.example.highload.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/response/")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

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
        List<ResponseDto> entityList = responseService.findAllForOrder(orderId);
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/all/{profileId}")
    @PreAuthorize("hasAnyAuthority('ARTIST')")
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getAllByProfile(@PathVariable int profileId){
        List<ResponseDto> entityList = responseService.findAllForProfile(profileId);
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@PathVariable int id){
        ResponseDto entity = responseService.findById(id);
        return ResponseEntity.ok(entity);
    }


}
