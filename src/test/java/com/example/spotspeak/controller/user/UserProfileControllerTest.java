package com.example.spotspeak.controller.user;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.controller.BaseControllerTest;
import com.example.spotspeak.entity.User;

import jakarta.transaction.Transactional;

public class UserProfileControllerTest
        extends BaseControllerTest {

    @Autowired
    private UserProfileController profileController;

    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        List<UserRepresentation> keycloakUsers = getKeycloakUsers();

        testUsers = new ArrayList<>();
        for (UserRepresentation keycloakUser : keycloakUsers) {
            User persistedUser = TestEntityFactory.createdPersistedUser(entityManager, keycloakUser);
            testUsers.add(persistedUser);

            getAccessTokenForUser(keycloakUser.getId());
        }

    }

    @Test
    @Transactional
    void contextLoads() {

    }

}
