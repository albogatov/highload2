package com.example.user.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="order-service")
public interface OrderRestConsumer {

    @GetMapping("/order/adminRemove")
    public void deleteOrders(@PathVariable int profileId);

}
