package com.clinic.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    @Value("${service.api-key}")
    private String apiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String serviceKey = request.getHeader("X-Service-Key");

        // No service key -> let normal JWT authentication continue
        if (serviceKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Service key exists, but is invalid
        if (!apiKey.equals(serviceKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Valid internal service authentication
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "doctor-service",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_SERVICE")
                        )
                );

        authentication.setDetails("internal-service");

        // Put service authentication into SecurityContext
        org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}