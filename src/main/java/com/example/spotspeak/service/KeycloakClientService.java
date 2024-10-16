package com.example.spotspeak.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.example.spotspeak.config.KeycloakClientConfiguration;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.exception.KeycloakClientException;
import com.example.spotspeak.exception.KeycloakException;
import com.example.spotspeak.exception.KeycloakServerException;
import com.example.spotspeak.exception.UserNotFoundException;
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

			user.setFirstName(user.getFirstName());
			user.setLastName(user.getLastName());
			getRealm().users().get(userId).update(user);
		} catch (ClientErrorException e) {
			int statusCode = e.getResponse().getStatus();
			String message = e.getMessage();
			throw new KeycloakClientException(message, statusCode);
		} catch (ServerErrorException e) {
			int statusCode = e.getResponse().getStatus();
			String message = e.getMessage();
			throw new KeycloakServerException(message, statusCode);
		} catch (Exception e) {
			String message = e.getMessage();
			throw new KeycloakException(message);
		}
	}

	public void deleteUser(String userId) {
		try {
			Response response = getRealm().users().delete(userId);
			if (response.getStatus() == 404) {
				throw new UserNotFoundException("User does not exist");
			}
			System.out.println("DELETED");
		} catch (ClientErrorException e) {
			int statusCode = e.getResponse().getStatus();
			String message = e.getMessage();
			throw new KeycloakClientException(message, statusCode);
		} catch (ServerErrorException e) {
			int statusCode = e.getResponse().getStatus();
			String message = e.getMessage();
			throw new KeycloakServerException(message, statusCode);
		} catch (UserNotFoundException e) {
			throw e;
		} catch (Exception e) {
			String message = e.getMessage();
			throw new KeycloakException(message);
		}
	}
}
