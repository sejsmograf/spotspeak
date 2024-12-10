package com.example.spotspeak.controller.user;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.User;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest
        extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    private final URI baseUri = URI.create("/api/users");

    private List<User> users;

    @BeforeEach
    void setUp() {
        users = new ArrayList<>();

        for (UserRepresentation user : getKeycloakUsers()) {
            User localUser = TestEntityFactory.createdPersistedUser(entityManager, user);
            users.add(localUser);
        }
    }

    @Test
    void contextLoads() {
        assertThat(userController).isNotNull();
    }

    @Test
    void search_shouldReturnMatchingUsers() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        mockMvc.perform(get(baseUri + "/search")
                .param("username", "testuser")
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk());
    }

    @Test
    void getUserProfile_shouldReturnUserProfile() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        String anotherUserId = users.get(1).getId().toString();

        mockMvc.perform(get(baseUri + "/profile/" + anotherUserId)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk());
    }
}
