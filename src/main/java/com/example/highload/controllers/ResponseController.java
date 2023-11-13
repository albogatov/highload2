package com.example.highload.controllers;

import com.example.highload.model.inner.Notification;
import com.example.highload.model.inner.Order;
import com.example.highload.model.inner.Response;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.ResponseRepository;
import com.example.highload.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/response/")
public class ResponseController {

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private ResponseService responseService;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(responseRepository.save(responseService.prepareEntity(data)) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save review, check data");
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
        List<Response> entityList = responseRepository.findAll();
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single")
    public ResponseEntity getById(@RequestBody IdDto data){
        Order entity = responseRepository.findById(data.getId()).get();
        return ResponseEntity.ok(responseService.prepareDto(entity));
    }


}
