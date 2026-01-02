package com.gym.apigateway.filter;

import com.gym.apigateway.config.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/trainees",
            "/api/trainers"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow open endpoints
        if (isOpenEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check for Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOpenEndpoint(String path, String method) {
        if (path.equals("/api/auth/login")) {
            return true;
        }
        if (method.equals("POST") && (path.equals("/api/trainees") || path.equals("/api/trainers"))) {
            return true;
        }
        return false;
    }
}
