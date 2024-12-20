package com.example.spotspeak.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import com.example.spotspeak.config.KeycloakClientConfiguration;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.exception.AttributeAlreadyExistsException;
import com.example.spotspeak.exception.KeycloakClientException;
import com.example.spotspeak.exception.PasswordChallengeFailedException;
import com.example.spotspeak.util.KeycloakClientBuilder;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;

@Service
public class KeycloakClientService {

    private KeycloakClientBuilder clientBuilder;
    private Keycloak adminClient;
    private String keycloakRealmName;

    public KeycloakClientService(KeycloakClientBuilder clientBuilder,
            KeycloakClientConfiguration config) {
        this.clientBuilder = clientBuilder;
        this.adminClient = clientBuilder.buildAdminClient();
        this.keycloakRealmName = config.realmName();
    }

    public void updatePassword(String userId, PasswordUpdateDTO dto) {
        try {
            UserResource user = getRealm().users().get(userId);
            String username = user.toRepresentation().getUsername();

            if (!verifyPassword(username, dto.currentPassword())) {
                throw new PasswordChallengeFailedException("Invalid current password");
            }

            CredentialRepresentation newCredentials = new CredentialRepresentation();
            newCredentials.setTemporary(false);
            newCredentials.setValue(dto.newPassword());

            user.resetPassword(newCredentials);
        } catch (ServerErrorException | ClientErrorException e) {
            handleClientError(e);
        } catch (PasswordChallengeFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakClientException("Keycloak client exception", e.getMessage());
        }
    }

    public void validatePasswordOrThrow(String userId, String password) {
        UserResource user = getRealm().users().get(userId);
        String username = user.toRepresentation().getUsername();
        if (!verifyPassword(username, password)) {
            throw new PasswordChallengeFailedException("Failed to validate password");
        }
    }

    public void updateUser(String userId, UserUpdateDTO updatedUserModel) {
        try {
            UserRepresentation userRepresentation = getRealm().users().get(userId).toRepresentation();

            validateUpdatePossible(userRepresentation, updatedUserModel);
            updateUserFields(userRepresentation, updatedUserModel);

            getRealm().users().get(userId).update(userRepresentation);
        } catch (ServerErrorException | ClientErrorException e) {
            handleClientError(e);
        } catch (AttributeAlreadyExistsException | PasswordChallengeFailedException e) {
            throw e;
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

    private void validateUpdatePossible(UserRepresentation user, UserUpdateDTO dto) {
        if (dto.email() != null && !user.getEmail().equals(dto.email())) {
            validateEmailUniqueOrThrow(dto.email());
        }
        if (dto.username() != null && !user.getUsername().equals(dto.username())) {
            validateUsernameUniqueOrThrow(dto.username());
        }
    }

    private void validateEmailUniqueOrThrow(String email) {
        if (checkEmailExists(email)) {
            throw new AttributeAlreadyExistsException("Email already exists");
        }
        if (checkUsernameExists(email)) {
            throw new AttributeAlreadyExistsException("Email already exists as username");
        }
    }

    private void validateUsernameUniqueOrThrow(String username) {
        if (checkUsernameExists(username)) {
            throw new AttributeAlreadyExistsException("Username already exists");
        }
        if (checkEmailExists(username)) {
            throw new AttributeAlreadyExistsException("Username already exists as email");
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

    private boolean verifyPassword(String username, String currentPassword) {
        try {
            Keycloak keycloakPasswordClient = clientBuilder.buildPasswordClient(username, currentPassword);
            keycloakPasswordClient.tokenManager().getAccessToken();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkEmailExists(String email) {
        return !getRealm().users().searchByEmail(email, true).isEmpty();
    }

    private boolean checkUsernameExists(String username) {
        return !getRealm().users().search(username, true).isEmpty();
    }

    private RealmResource getRealm() {
        return adminClient.realm(keycloakRealmName);
    }
}
