package com.example.user.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="order-service")
public interface OrderRestConsumer {

    @GetMapping("/order/adminRemove")
    ResponseEntity<?> deleteOrders(@PathVariable int profileId);

}
