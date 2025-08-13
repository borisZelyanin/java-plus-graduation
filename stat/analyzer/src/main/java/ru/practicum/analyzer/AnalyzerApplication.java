package ru.practicum.analyzer;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = "ru.practicum.analyzer")
public class AnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApplication.class, args);
    }
}