package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.exception.AttributeAlreadyExistsException;
import com.example.spotspeak.exception.KeycloakClientException;
import com.example.spotspeak.exception.PasswordChallengeFailedException;

public class KeycloakClientServiceIntegrationTest
        extends BaseTestWithKeycloak {

    @Autowired
    private KeycloakClientService keycloakClientService;

    @Test
    void updatePassword_shouldThrow_whenUserNotFound() {
        String nonExistingUserId = "nonexistent";
        PasswordUpdateDTO dto = new PasswordUpdateDTO("currentnotvalid", "new");

        assertThrows(KeycloakClientException.class,
                () -> keycloakClientService.updatePassword(nonExistingUserId, dto));
    }

    @Test
    void updatePassword_shouldThrow_whenPasswordDoesntMatch() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        String notValid = user.getUsername() + "notvalid";
        PasswordUpdateDTO dto = new PasswordUpdateDTO(notValid, "newpassword");

        assertThrows(PasswordChallengeFailedException.class,
                () -> keycloakClientService.updatePassword(userId, dto));
    }

    @Test
    void updatePassword_shouldUpdatePassword() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        String password = user.getUsername(); // test user has password equal to username
        String newPassword = "newpassword";
        PasswordUpdateDTO dto = new PasswordUpdateDTO(password, newPassword);

        keycloakClientService.updatePassword(userId, dto);
        // not throwing anything means update successful

        // clean up ( restore original password )
        PasswordUpdateDTO resetDto = new PasswordUpdateDTO(newPassword, password);
        keycloakClientService.updatePassword(userId, resetDto);
    }

    @Test
    void validatePasswordOrThrow_shouldThrow_whenPasswordDoesntMatch() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        String invalidPassword = user.getUsername() + "notvalid"; // test user has password equal to username

        assertThrows(PasswordChallengeFailedException.class,
                () -> keycloakClientService.validatePasswordOrThrow(userId, invalidPassword));
    }

    @Test
    void updateUser_shouldThrow_whenUsernameExists() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        UserRepresentation anotherUser = users.get(1);
        String anotherUsername = anotherUser.getUsername();
        UserUpdateDTO updatedUserModel = UserUpdateDTO.builder()
                .username(anotherUsername)
                .build();

        assertThrows(AttributeAlreadyExistsException.class,
                () -> keycloakClientService.updateUser(userId, updatedUserModel));
    }

    @Test
    void updateUser_shouldThrow_whenUsernameExistsAsEmail() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        UserRepresentation anotherUser = users.get(1);
        String anotherEmail = anotherUser.getEmail();
        UserUpdateDTO updatedUserModel = UserUpdateDTO.builder()
                .username(anotherEmail)
                .build();

        assertThrows(AttributeAlreadyExistsException.class,
                () -> keycloakClientService.updateUser(userId, updatedUserModel));
    }

    @Test
    void updateUser_shouldUpdateUser() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        String oldUsername = user.getUsername();
        String oldFirstName = user.getFirstName();
        String oldLastName = user.getLastName();
        String oldEmail = user.getEmail();

        UserUpdateDTO updatedUserModel = UserUpdateDTO.builder()
                .firstName("newFirstname")
                .lastName("newLastName")
                .email("newEmail@email.com")
                .username("newusername")
                .build();

        keycloakClientService.updateUser(userId, updatedUserModel);
        UserRepresentation updatedUser = getKeycloakUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("newusername");

        // clean after test, restore original values
        UserUpdateDTO resetModel = UserUpdateDTO.builder()
                .firstName(oldFirstName)
                .lastName(oldLastName)
                .email(oldEmail)
                .username(oldUsername)
                .build();
        keycloakClientService.updateUser(userId, resetModel);
        updatedUser = getKeycloakUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo(oldUsername);
    }

    @Test
    void updateUser_shouldThrow_whenEmailExists() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        UserRepresentation anotherUser = users.get(1);
        String anotherEmail = anotherUser.getEmail();
        UserUpdateDTO updatedUserModel = UserUpdateDTO.builder()
                .email(anotherEmail)
                .build();

        assertThrows(AttributeAlreadyExistsException.class,
                () -> keycloakClientService.updateUser(userId, updatedUserModel));
    }

    @Test
    void updateUser_shouldThrow_whenEmailExistsAsUsername() {
        List<UserRepresentation> users = getKeycloakUsers();
        UserRepresentation user = users.get(0);
        String userId = user.getId();
        UserRepresentation anotherUser = users.get(1);
        String anotherEmail = anotherUser.getUsername();
        UserUpdateDTO updatedUserModel = UserUpdateDTO.builder()
                .email(anotherEmail)
                .build();

        assertThrows(AttributeAlreadyExistsException.class,
                () -> keycloakClientService.updateUser(userId, updatedUserModel));
    }

    @Test
    void deleteUser_shouldThrow_whenUserIdDoesntExist() {
        String userId = "doestnexist";

        assertThrows(KeycloakClientException.class,
                () -> keycloakClientService.deleteUser(userId));
    }
}
