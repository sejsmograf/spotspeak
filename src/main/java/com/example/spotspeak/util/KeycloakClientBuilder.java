package com.example.spotspeak.util;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.spotspeak.config.KeycloakClientConfiguration;

@Component
public class KeycloakClientBuilder {
    private final String CLIENT_CREDENTIALS_GRANT_TYPE = OAuth2Constants.CLIENT_CREDENTIALS;
    private final String PASSWORD_GRANT_TYPE = OAuth2Constants.PASSWORD;

    @Autowired
    private KeycloakClientConfiguration config;

    public Keycloak buildAdminClient() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .realm(config.realmName())
                .serverUrl(config.serverUrl())
                .clientId(config.clientId())
                .clientSecret(config.clientSecret())
                .grantType(CLIENT_CREDENTIALS_GRANT_TYPE)
                .build();

        return keycloak;
    }

    public Keycloak buildPasswordClient(String username, String password) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .realm(config.realmName())
                .serverUrl(config.serverUrl())
                .clientId(config.clientId())
                .clientSecret(config.clientSecret())
                .grantType(PASSWORD_GRANT_TYPE)
                .username(username)
                .password(password)
                .build();

        return keycloak;
    }
}
