package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestHighloadApplication {

    public static void main(String[] args) {
        SpringApplication.from(NotificationServiceApplication::main).with(TestHighloadApplication.class).run(args);
    }

}
