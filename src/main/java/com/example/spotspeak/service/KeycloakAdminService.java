package com.example.spotspeak.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.example.spotspeak.config.KeycloakAdminClientConfiguration;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.util.KeycloakClientBuilder;

@Service
public class KeycloakAdminService {

	public Keycloak client;
	private String keycloakRealmName;

	public KeycloakAdminService(KeycloakClientBuilder clientBuilder, KeycloakAdminClientConfiguration config) {
		this.client = clientBuilder.build();
		this.keycloakRealmName = config.realmName();
	}

	private RealmResource getRealm() {
		return client.realm(keycloakRealmName);
	}

	public void updateUser(String userId, UserUpdateDTO updatedUserModel) {
		UserRepresentation user = getRealm()
				.users().get(userId).toRepresentation();

		user.setFirstName(user.getFirstName());
		user.setLastName(user.getLastName());
		getRealm().users().get(userId).update(user);
	}
}
