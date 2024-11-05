package com.example.spotspeak.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.exception.KeycloakClientException;
import com.example.spotspeak.exception.PasswordChallengeFailedException;

public class KeycloakClientServiceIntegrationTest
        extends BaseTestWithKeycloak {

    @Autowired
    private KeycloakClientService keycloakClientService;

    @Test
    void updatePassword_shouldThrowKeycloakClientException_whenUserNotFound() {
        String nonExistingUserId = "nonexistent";
        PasswordUpdateDTO dto = new PasswordUpdateDTO("currentnotvalid", "new");

        assertThrows(KeycloakClientException.class, () -> keycloakClientService.updatePassword(nonExistingUserId, dto));
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
}
