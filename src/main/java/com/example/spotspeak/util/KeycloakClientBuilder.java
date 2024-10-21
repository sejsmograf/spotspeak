package com.example.spotspeak.util;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.spotspeak.config.KeycloakClientConfiguration;

@Component
public class KeycloakClientBuilder {
    private final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";

    @Autowired
    private KeycloakClientConfiguration config;

    public Keycloak build() {
        var keycloak = KeycloakBuilder.builder()
                .realm(config.realmName())
                .serverUrl(config.serverUrl())
                .clientId(config.clientId())
                .clientSecret(config.clientSecret())
                .grantType(CLIENT_CREDENTIALS_GRANT_TYPE)
                .build();

        return keycloak;
    }
}
