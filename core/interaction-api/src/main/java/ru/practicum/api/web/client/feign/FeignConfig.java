package ru.practicum.api.web.client.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignConfig {

    @Bean
    public Encoder feignEncoder(ObjectMapper objectMapper) {
        return new SpringEncoder(() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper)));
    }

    @Bean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        return new SpringDecoder(() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper)));
    }
}