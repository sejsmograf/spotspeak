package com.example.spotspeak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakClientConfiguration(
                String clientId,
                String clientSecret,
                String serverUrl,
                String realmName) {

}
