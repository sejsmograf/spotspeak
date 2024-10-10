package com.example.spotspeak.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakConfigurationProperties(
		String clientId,
		String clientSecret,
		String serverUrl,
		String realmName) {
}
