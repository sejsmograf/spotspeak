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
			UserRepresentation user = getRealm().users().get(userId).toRepresentation();
			if (user == null) {
				throw new KeycloakClientException("Keycloak user not found");
			}

			if (updatedUserModel.email() != null
					&& !updatedUserModel.email().equals(user.getEmail())
					&& checkEmailExists(updatedUserModel.email())) {
				throw new KeycloakClientException("Email already exists");
			}

			if (updatedUserModel.username() != null
					&& !updatedUserModel.username().equals(user.getUsername())
					&& checkUsernameExists(updatedUserModel.username())) {
				throw new KeycloakClientException("Username already exists");
			}

			updateUserFields(user, updatedUserModel);
			getRealm().users().get(userId).update(user);
		} catch (ServerErrorException | ClientErrorException e) {
			handleClientError(e);
		} catch (Exception e) {
			throw new KeycloakClientException("Keycloak client exception", e.getMessage());
		}
	}

	private void updateUserFields(UserRepresentation user, UserUpdateDTO updatedUserModel) {
		if (updatedUserModel.firstName() != null) {
			user.setFirstName(updatedUserModel.firstName());
		}
		if (updatedUserModel.lastName() != null) {
			user.setLastName(updatedUserModel.lastName());
		}
		if (updatedUserModel.email() != null) {
			user.setEmail(updatedUserModel.email());
		}
		if (updatedUserModel.username() != null) {
			user.setUsername(updatedUserModel.username());
		}
	}

	private void handleClientError(Exception e) {
		int statusCode = ((ClientErrorException) e).getResponse().getStatus();
		String message = "Error updating Keycloak user: "
				+ (e instanceof ServerErrorException ? "Keycloak server connection error"
						: "Keycloak client connection error");
		throw new KeycloakClientException(message, statusCode + e.getMessage());
	}

	private boolean checkEmailExists(String email) {
		return !getRealm().users().searchByEmail(email, true).isEmpty();
	}

	private boolean checkUsernameExists(String username) {
		return !getRealm().users().search(username).isEmpty();
	}

	public void deleteUser(String userId) {
		Response response = getRealm().users().delete(userId);
		if (response.getStatus() != 200 && response.getStatus() != 204) {
			throw new KeycloakClientException("Error deleting keycloak user",
					"Response status: " + response.getStatus());
		}
	}

}
