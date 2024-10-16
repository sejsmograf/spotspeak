package com.example.spotspeak.service;

import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;

import com.example.spotspeak.util.KeycloakClientBuilder;

@Service
public class KeycloakAdminService {

	public Keycloak client;

	public KeycloakAdminService(KeycloakClientBuilder clientBuilder) {
		this.client = clientBuilder.build();
	}
}
