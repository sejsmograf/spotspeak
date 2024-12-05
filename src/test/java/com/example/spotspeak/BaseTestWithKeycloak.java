package com.example.spotspeak;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.example.spotspeak.service.BaseServiceIntegrationTest;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@ActiveProfiles("test")
public abstract class BaseTestWithKeycloak extends BaseServiceIntegrationTest {

    public static KeycloakContainer keycloak = new KeycloakContainer()
            .withRealmImportFile("testrealm.json");

    static {
        keycloak.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.realm-name", () -> "testrealm");
        registry.add("keycloak.server-url", keycloak::getAuthServerUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/testrealm");
        registry.add("keycloak.client-id", () -> "spring-backend");
        registry.add("keycloak.client-secret", () -> "fsIsl8ebo5gU8mCZnzuKjzA1GcE63sFT");
    }

    /*
     * This method is used to get all the users from the test realm in
     * Keycloak container for testing purposes.
     * Currently there are 5 users in the test realm.
     * Each user is called testuserX where X is a number from 1 to 5.
     * The password for each user is equal to their username.
     * Important thing to notice is that keycloak operations
     * are not rolled back after each test method.
     */
    protected List<UserRepresentation> getKeycloakUsers() {
        return keycloak.getKeycloakAdminClient()
                .realm("testrealm")
                .users()
                .list();
    }

    protected String getAccessToken(String username) {
        Keycloak userKeycloak = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("testrealm")
                .clientId("spring-backend")
                .clientSecret("fsIsl8ebo5gU8mCZnzuKjzA1GcE63sFT")
                .username(username)
                .password(username)
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        String accessToken = userKeycloak.tokenManager().getAccessTokenString();
        System.out.println(
                "Access token for user " + username + ": " + accessToken);
        return accessToken;
    }
}
