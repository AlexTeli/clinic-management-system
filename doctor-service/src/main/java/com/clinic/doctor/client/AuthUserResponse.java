package com.clinic.doctor.client;

public record AuthUserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}