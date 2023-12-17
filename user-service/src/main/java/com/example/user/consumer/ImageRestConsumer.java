package com.example.user.consumer;

import com.example.user.model.inner.Profile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="image-service")
public interface ImageRestConsumer {

    @GetMapping("/image/adminRemove")
    ResponseEntity<?> deleteImages(@PathVariable int profileId);

}
