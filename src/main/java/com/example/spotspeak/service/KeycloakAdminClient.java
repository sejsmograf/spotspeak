package com.example.spotspeak.service;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class KeycloakAdminClient {

	private Keycloak keycloak;
	private KeycloakConfigurationProperties config;

	public KeycloakAdminClient(KeycloakConfigurationProperties config) {
		this.config = config;
	}

	@PostConstruct
	private void init() {
		keycloak = KeycloakBuilder.builder()
				.serverUrl(config.serverUrl())
				.realm(config.realmName())
				.clientId(config.clientId())
				.clientSecret(config.clientSecret())
				.grantType("client_credentials")
				.build();
	}

	public List<UserRepresentation> getUsers() {
		var users = keycloak.realm(config.realmName()).users().list();
		return users;
	}

}
