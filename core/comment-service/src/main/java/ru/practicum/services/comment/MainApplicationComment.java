package ru.practicum.services.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "ru.practicum.api.web.client.feign")
@SpringBootApplication(scanBasePackages = "ru.practicum.services.comment")
public class MainApplicationComment {
    public static void main(String[] args) {
        SpringApplication.run(MainApplicationComment.class,args);
    }
}
