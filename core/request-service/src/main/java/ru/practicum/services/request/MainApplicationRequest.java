package ru.practicum.services.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "ru.practicum.api.web.client.feign")
@SpringBootApplication(scanBasePackages = "ru.practicum.services.request")
public class MainApplicationRequest {
    public static void main(String[] args) {
        SpringApplication.run(MainApplicationRequest.class,args);
    }
}