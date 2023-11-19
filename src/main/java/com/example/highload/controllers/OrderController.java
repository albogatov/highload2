package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.model.network.ReviewDto;
import com.example.highload.services.ImageService;
import com.example.highload.services.OrderService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OrderController {

    private OrderService orderService;
    private ImageService imageService;
    private PaginationHeadersCreator paginationHeadersCreator;
    private final DataTransformer dataTransformer;

    @CrossOrigin
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity save(@RequestBody OrderDto data){
        if(orderService.saveOrder(data) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save order, check data");
    }

    @CrossOrigin
    @PostMapping("/update/{orderId}")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity update(@RequestBody OrderDto data, @PathVariable int orderId){
        if(orderService.updateOrder(data, orderId) != null)
            return ResponseEntity.ok("");
        else return ResponseEntity.badRequest().body("Couldn't save order, check data");
    }

    @CrossOrigin
    @PostMapping("/respond")
    @PreAuthorize("hasAuthority('ARTIST')")
    // todo from sonja: Should be in Response controller & ResponseDto data as request body ; orderId as path variable
    public ResponseEntity registerOrderResponse(@RequestBody OrderDto data){
        return null;
    }

    @CrossOrigin
    @GetMapping("/all/user/{userId}/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllUserOrders(@PathVariable int userId, @PathVariable int page){

        Pageable pageable = PageRequest.of(page, 50);
        Page<Order> entityList = orderService.getUserOrders(userId, pageable);

        HttpHeaders responseHeaders = paginationHeadersCreator.endlessSwipeHeadersCreate(entityList);
        // "findAll в виде бесконечной прокрутки без указания общего количества записей"

        return ResponseEntity.ok().headers(responseHeaders).body(dataTransformer.orderListToDto(entityList.getContent()));

    }

    @CrossOrigin
    @GetMapping("/open/user/{userId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    // todo: "findAll в виде бесконечной прокрутки без указания общего количества записей"
    public ResponseEntity getAllUserOpenOrders(@PathVariable int userId){
        List<Order> entityList = orderService.getUserOpenOrders(userId);
        List<OrderDto> dtoList = dataTransformer.orderListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }

    @CrossOrigin
    @GetMapping("/single/{orderId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@RequestBody int id){
        Order entity = orderService.getOrderById(id);
        return ResponseEntity.ok(dataTransformer.orderToDto(entity));
    }


    @CrossOrigin
    @GetMapping("/single/{orderId}/images")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    // todo: "запрос, который вернет findAll с пагинацией и с указанием общего количества записей в http хедере."
    public ResponseEntity getOrderImages(@RequestBody int id){
        List<Image> entityList = imageService.findAllOrderImages(id);
        List<ImageDto> dtoList = dataTransformer.imageListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }


    @CrossOrigin
    @GetMapping("/all/tag/{page}")
    @PreAuthorize("hasAnyAuthority('ARTIST')")
    public ResponseEntity getAllOrdersByTags(@RequestBody List<Integer> tags, @PathVariable int page){

        Pageable pageable = PageRequest.of(page, 50);
        Page<Order> entityList = orderService.getOrdersByTags(tags, pageable);

        HttpHeaders responseHeaders = paginationHeadersCreator.endlessSwipeHeadersCreate(entityList);
        //"findAll в виде бесконечной прокрутки без указания общего количества записей"

        return ResponseEntity.ok().headers(responseHeaders).body(dataTransformer.orderListToDto(entityList.getContent()));
    }


    @CrossOrigin
    @GetMapping("/open/tag/{page}")
    @PreAuthorize("hasAnyAuthority('ARTIST')")
    public ResponseEntity getAllOpenOrdersByTags(@RequestBody List<Integer> tags, @PathVariable int page){

        Pageable pageable = PageRequest.of(page, 50);
        Page<Order> entityList = orderService.getOpenOrdersByTags(tags, pageable);

        HttpHeaders responseHeaders = paginationHeadersCreator.endlessSwipeHeadersCreate(entityList);
        //"findAll в виде бесконечной прокрутки без указания общего количества записей"

        return ResponseEntity.ok().headers(responseHeaders).body(dataTransformer.orderListToDto(entityList.getContent()));
    }


    @CrossOrigin
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    // todo: "findAll в виде бесконечной прокрутки без указания общего количества записей"
    public ResponseEntity getAllOrders(){
        List<Order> entityList = orderService.getAllOrders();
        List<OrderDto> dtoList = dataTransformer.orderListToDto(entityList);
        return ResponseEntity.ok(dtoList);
    }




}
