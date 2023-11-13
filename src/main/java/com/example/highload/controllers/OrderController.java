package com.example.highload.controllers;

import com.example.highload.model.inner.Order;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/order/")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @CrossOrigin
    @PostMapping("/save")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(orderRepository.save(orderService.prepareEntity(data)) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save order, check data");
    }

    @CrossOrigin
    @PostMapping("/respond")
    public ResponseEntity registerOrderResponse(@RequestBody OrderDto data){
        // TODO: Should this be here or in Response controller?
    }

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity getAllQueries(){
        List<Order> entityList = orderRepository.findAll();
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single")
    public ResponseEntity getById(@RequestBody IdDto data){
        Order entity = orderRepository.findById(data.getId()).get();
        return ResponseEntity.ok(orderService.prepareDto(entity));
    }

}
