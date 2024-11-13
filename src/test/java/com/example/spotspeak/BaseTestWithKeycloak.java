package com.example.spotspeak;

import java.util.List;

import org.keycloak.admin.client.resource.UserResource;
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

    protected void getAccessTokenForUser(String userId) {
        System.out.print("IMPERSONATING USER");
        UserResource user = keycloak.getKeycloakAdminClient()
                .realm("testrealm")
                .users()
                .get(userId);

        var impersonation = user.impersonate();

        for (var key : impersonation.keySet()) {
            System.out.println("KEY");
            System.out.println(key);
            System.out.println("VALUE");
            System.out.println(impersonation.get(key));
        }
    }
}
