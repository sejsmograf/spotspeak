package com.example.spotspeak.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.dto.ChallengeRequestDTO;
import com.example.spotspeak.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserProfileControllerTest
        extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserProfileController profileController;

    private final URI baseUri = URI.create("/api/users/me");

    private List<User> users;

    private JwtRequestPostProcessor getMockAccessToken(String userId) {
        return jwt().jwt(jwt -> jwt.subject(userId));
    }

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
        assertThat(profileController).isNotNull();
    }

    @Test
    void getUserProfile_shouldReturnUserProfile() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        mockMvc.perform(get(baseUri)
                .with(getMockAccessToken(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(userId))
                .andExpect(jsonPath("username").value(user.getUsername()))
                .andExpect(jsonPath("email").value(user.getEmail()));
    }

    @Test
    void getUserProfile_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get(baseUri)
                .with(jwt().jwt(jwt -> jwt.subject("nonexistent"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateChallenge_shouldReturnToken_ifCorrectPassword() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        String password = user.getUsername(); // testusers have the same username and password
        String body = new ObjectMapper().writeValueAsString(new ChallengeRequestDTO(password));

        mockMvc.perform(post(baseUri + "/generate-challenge")
                .header("Content-Type", "application/json")
                .content(body)
                .with(getMockAccessToken(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").isNotEmpty());
    }

    @Test
    void generateChallenge_shouldReturnToken_whenNoPasswordProvidedAndIdentityProvider()
            throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        String password = null; // user with google identity provider doesn't need password
        String body = new ObjectMapper().writeValueAsString(new ChallengeRequestDTO(password));

        mockMvc.perform(post(baseUri + "/generate-challenge")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId).claim("identity_provider", "google"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").isNotEmpty());
    }

    @Test
    void generateChallenge_shouldReturn403_whenPasswordNotCorrect() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        String password = "not" + user.getUsername();
        String body = new ObjectMapper().writeValueAsString(new ChallengeRequestDTO(password));

        mockMvc.perform(post(baseUri + "/generate-challenge")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isForbidden());
    }
}
