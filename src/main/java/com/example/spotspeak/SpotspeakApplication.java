package com.example.spotspeak;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.spotspeak.config.KeycloakClientConfiguration;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties(KeycloakClientConfiguration.class)
@EnableScheduling
public class SpotspeakApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpotspeakApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));
    }
}
