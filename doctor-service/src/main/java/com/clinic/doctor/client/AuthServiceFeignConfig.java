package com.clinic.doctor.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthServiceFeignConfig {

    @Bean
    public RequestInterceptor authServiceRequestInterceptor(
            @Value("${services.auth.api-key}") String apiKey
    ) {
        return requestTemplate ->
                requestTemplate.header(
                        "X-Service-Key",
                        apiKey
                );
    }
}