package com.example.highload;

import com.example.image.ImageServiceApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestHighloadApplication {

    public static void main(String[] args) {
        SpringApplication.from(ImageServiceApplication::main).with(TestHighloadApplication.class).run(args);
    }

}
