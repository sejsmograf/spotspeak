package com.example.spotspeak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Stateless API doesn't need CSRF
        http.csrf(
                csrf -> csrf.disable());

        // JWT based authentication doesnt need sessions
        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Permit access to Swagger UI and API documentation
        http.authorizeHttpRequests(
                authorize -> authorize
                        .requestMatchers("/api/docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated());

        // Disable form login
        http.formLogin(
                formLogin -> formLogin.disable());

        // Enable OAuth2 resource server
        http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

}
