package ru.practicum.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonTimeConfig {

    private static final DateTimeFormatter LDT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            JavaTimeModule m = new JavaTimeModule();
            m.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(LDT_FMT));
            m.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(LDT_FMT));
            builder.modules(m);
        };
    }
}