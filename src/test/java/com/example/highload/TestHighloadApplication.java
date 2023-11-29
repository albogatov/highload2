package com.example.highload;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration(proxyBeanMethods = false)
public class TestHighloadApplication {

    public static void main(String[] args) {
        SpringApplication.from(HighloadApplication::main).with(TestHighloadApplication.class).run(args);
    }

}
