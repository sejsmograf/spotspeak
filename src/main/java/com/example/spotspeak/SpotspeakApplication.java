package com.example.spotspeak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.spotspeak.config.KeycloakClientConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(KeycloakClientConfiguration.class)
public class SpotspeakApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpotspeakApplication.class, args);
    }
}
