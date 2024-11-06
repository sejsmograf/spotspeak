
package com.example.spotspeak.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestOutcomeLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestOutcomeLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        filterChain.doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String clientIp = request.getHeader("X-Forwarded-For") == null
                ? request.getRemoteAddr()
                : request.getHeader("X-Forwarded-For");
        String endpoint = request.getRequestURI();

        if (auth != null && auth.isAuthenticated()) {
            String userId = auth.getName();
            logger.info("User {} accessed endpoint {} from IP {}", userId, endpoint, clientIp);
        } else {
            logger.warn("Unauthenticated user accessed endpoint {} from IP {}", endpoint, clientIp);
        }
    }

}
