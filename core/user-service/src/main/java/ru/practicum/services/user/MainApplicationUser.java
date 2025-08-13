package ru.practicum.services.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "ru.practicum.services.user")

public class MainApplicationUser {
    public static void main(String[] args) {
        SpringApplication.run(MainApplicationUser.class,args);
    }
}