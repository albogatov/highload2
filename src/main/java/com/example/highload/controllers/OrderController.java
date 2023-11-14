package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/app/order/")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @CrossOrigin
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(orderService.saveOrder(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save order, check data");
    }

    @CrossOrigin
    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity update(@RequestBody OrderDto data, @PathVariable int id){
        if(orderService.updateOrder(data, id) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save order, check data");
    }

    @CrossOrigin
    @PostMapping("/respond")
    @PreAuthorize("hasAuthority('ARTIST')")
    public ResponseEntity registerOrderResponse(@RequestBody OrderDto data){
        // TODO: Should this be here or in Response controller?
        return null;
    }

    @CrossOrigin
    @GetMapping("/all/{userId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllQueries(@PathVariable int id){
        List<OrderDto> entityList = orderService.getUserOrders(id);
        return ResponseEntity.ok(entityList);
    }

    @CrossOrigin
    @GetMapping("/single/{orderId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@RequestBody int id){
        OrderDto entity = orderService.getOrderById(id);
        return ResponseEntity.ok(entity);
    }

    @CrossOrigin
    @GetMapping("/single/{orderId}/images")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getOrderImages(@RequestBody int id){
        List<ImageDto> entity = orderService.getImagesForOrder(id);
        return ResponseEntity.ok(entity);
    }

}
