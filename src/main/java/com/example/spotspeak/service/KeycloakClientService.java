package com.example.spotspeak.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import com.example.spotspeak.config.KeycloakClientConfiguration;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.exception.KeycloakClientException;
import com.example.spotspeak.util.KeycloakClientBuilder;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;

@Service
public class KeycloakClientService {

	public Keycloak client;
	private String keycloakRealmName;

	public KeycloakClientService(KeycloakClientBuilder clientBuilder,
			KeycloakClientConfiguration config) {
		this.client = clientBuilder.build();
		this.keycloakRealmName = config.realmName();
	}

	private RealmResource getRealm() {
		return client.realm(keycloakRealmName);
	}

	public void updateUser(String userId, UserUpdateDTO updatedUserModel) {
		try {
			UserRepresentation user = getRealm()
					.users().get(userId).toRepresentation();

			if (user == null) {
				throw new KeycloakClientException("Keyclaok user not found");
			}

			user.setFirstName(updatedUserModel.firstName());
			user.setLastName(updatedUserModel.lastName());
			getRealm().users().get(userId).update(user);
		} catch (ServerErrorException | ClientErrorException e) {
			int statusCode = e.getResponse().getStatus();
			String message = "Error updating keycloak user ";
			message += e instanceof ServerErrorException ? "Keyclaok server connection error"
					: "Keycloak client connection error";

			throw new KeycloakClientException(message, statusCode + e.getMessage());
		} catch (Exception e) {
			throw new KeycloakClientException("Keycloak client exception", e.getMessage());
		}
	}

	public void deleteUser(String userId) {
		Response response = getRealm().users().delete(userId);
		if (response.getStatus() != 200 && response.getStatus() != 204) {
			throw new KeycloakClientException("Error deleting keycloak user",
					"Response status: " + response.getStatus());
		}
	}

}
