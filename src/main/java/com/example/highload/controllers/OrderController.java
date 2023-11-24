package com.example.highload.controllers;

import com.example.highload.model.inner.Image;
import com.example.highload.model.inner.Order;
import com.example.highload.model.network.ImageDto;
import com.example.highload.model.network.OrderDto;
import com.example.highload.services.ImageService;
import com.example.highload.services.OrderService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
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

    private final OrderService orderService;
    private final ImageService imageService;
    private final PaginationHeadersCreator paginationHeadersCreator;
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
    @GetMapping("/open/user/{userId}/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllUserOpenOrders(@PathVariable int userId, @PathVariable int page){
        Pageable pageable = PageRequest.of(page, 50);
        Page<Order> entityList = orderService.getUserOpenOrders(userId, pageable);
        List<OrderDto> dtoList = dataTransformer.orderListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.endlessSwipeHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }

    @CrossOrigin
    @GetMapping("/single/{orderId}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getById(@RequestBody int id){
        Order entity = orderService.getOrderById(id);
        return ResponseEntity.ok(dataTransformer.orderToDto(entity));
    }


    @CrossOrigin
    @GetMapping("/single/{orderId}/images/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getOrderImages(@RequestBody int id, @PathVariable int page){
        Pageable pageable = PageRequest.of(page, 50);
        Page<Image> entityList = imageService.findAllOrderImages(id, pageable);
        List<ImageDto> dtoList = dataTransformer.imageListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
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
    @GetMapping("/all/{page}")
    @PreAuthorize("hasAnyAuthority('CLIENT', 'ARTIST')")
    public ResponseEntity getAllOrders(@PathVariable int page){
        Pageable pageable = PageRequest.of(page, 50);
        Page<Order> entityList = orderService.getAllOrders(pageable);
        List<OrderDto> dtoList = dataTransformer.orderListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.endlessSwipeHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }


    // TODO TAGS ADD/DELETE (<=10 tags per order)

}
