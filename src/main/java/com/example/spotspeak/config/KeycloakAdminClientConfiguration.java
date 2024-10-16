package com.example.spotspeak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakAdminClientConfiguration(
        String clientId,
        String clientSecret,
        String serverUrl,
        String realmName) {

}
