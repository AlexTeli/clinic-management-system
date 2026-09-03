package com.clinic.doctor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "auth-service",
        url = "${services.auth.url}",
        configuration = AuthServiceFeignConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/users/{id}")
    AuthUserResponse getUserById(
            @PathVariable Long id
    );
}