package com.clinic.doctor.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // Doctor profiles - everyone authenticated can view
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/doctors",
                                "/doctors/{id}",
                                "/doctors/user/{userId}",
                                "/doctors/*/experiences",
                                "/doctors/*/studies",
                                "/doctors/*/experiences/*",
                                "/doctors/*/studies/*"
                        ).hasAnyRole("USER", "DOCTOR", "ADMIN")

                        // Doctor profile management - ADMIN only
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/doctors"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/doctors/*"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/doctors/*"
                        ).hasRole("ADMIN")

                        // Experience management - DOCTOR or ADMIN
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/doctors/*/experiences"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/doctors/*/experiences/*"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/doctors/*/experiences/*"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        // Study management - DOCTOR or ADMIN
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/doctors/*/studies"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/doctors/*/studies/*"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/doctors/*/studies/*"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}