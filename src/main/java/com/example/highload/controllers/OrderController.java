package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.IdDto;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.repos.OrderRepository;
import com.example.highload.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
        // todo from sonja: Should be in Response controller & ResponseDto data as request body
        return null;
    }

    @CrossOrigin
    @GetMapping("/all/{userId}/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllOrders(@PathVariable int userId, @PathVariable int page){

        Pageable pageable = PageRequest.of(page, 50);
        Page<OrderDto> entityList = orderService.getUserOrders(userId, pageable);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("app-current-page-num", String.valueOf(page));
        responseHeaders.set("app-page-has-next", String.valueOf(entityList.hasNext()));

        // todo: "findAll в виде бесконечной прокрутки без указания общего количества записей"

        return ResponseEntity.ok().headers(responseHeaders).body(entityList.getContent());

    }

    @CrossOrigin
    @GetMapping("/open/{userId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllOpenOrders(@PathVariable int userId){
        List<OrderDto> entityList = orderService.getUserOpenOrders(userId);
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
