package ru.practicum.main.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "ru.practicum")
public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class,args);
    }
}
