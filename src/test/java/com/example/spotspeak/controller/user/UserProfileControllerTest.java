package com.example.spotspeak.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.constants.FileUploadConsants;
import com.example.spotspeak.dto.ChallengeRequestDTO;
import com.example.spotspeak.dto.ChallengeResponseDTO;
import com.example.spotspeak.dto.FcmTokenDTO;
import com.example.spotspeak.dto.NotificationPreferencesDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.KeycloakClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserProfileControllerTest
        extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserProfileController profileController;

    @Autowired
    KeycloakClientService keycloakClientService;

    private final URI baseUri = URI.create("/api/users/me");

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
        assertThat(profileController).isNotNull();
    }

    @Test
    void getUserProfile_shouldReturnUserProfile() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        mockMvc.perform(get(baseUri)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
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
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
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

    @Test
    void updateProfile_shouldReturn403_whenInvalidPasswordToken() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        UserUpdateDTO dto = UserUpdateDTO.builder()
                .passwordChallengeToken("invalid")
                .username("newusername")
                .build();
        String body = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(put(baseUri)
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_shouldUpdateProfile_whenValidPasswordTokenProvided() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = users.get(0);
        String username = user.getUsername();
        String userId = user.getId().toString();
        String password = username;
        String body = mapper.writeValueAsString(new ChallengeRequestDTO(password));

        MockHttpServletResponse response = mockMvc.perform(post(baseUri + "/generate-challenge")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").isNotEmpty())
                .andReturn().getResponse();

        ChallengeResponseDTO responseDTO = mapper.readValue(response.getContentAsByteArray(),
                ChallengeResponseDTO.class);
        String token = responseDTO.token();

        UserUpdateDTO dto = UserUpdateDTO.builder()
                .passwordChallengeToken(token)
                .email("newemail@gmail.com")
                .build();
        body = mapper.writeValueAsString(dto);

        mockMvc.perform(put(baseUri)
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfilePicture_shouldUpdate_whenValidFileProvided() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 100);

        mockMvc.perform(multipart(baseUri + "/picture")
                .file(file)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        User retrievedUser = entityManager.find(User.class, UUID.fromString(userId));
        assertThat(retrievedUser.getProfilePicture()).isNotNull();
    }

    @Test
    void updateProfilePicture_shouldReturn400_whenInvalidFileType() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("invlaid/jpg", 100);

        mockMvc.perform(multipart(baseUri + "/picture")
                .file(file)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();
    }

    @Test
    void updateProfilePicture_shouldReturn400_whenFileTooBig() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        int size = (int) FileUploadConsants.PROFILE_PICTURE_MAX_SIZE + 1;
        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", size);

        mockMvc.perform(multipart(baseUri + "/picture")
                .file(file)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();
    }

    @Test
    void deleteProfilePictureShouldWork_whenUserHasNoProfilePicture() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 100);

        mockMvc.perform(multipart(baseUri + "/picture")
                .file(file)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        mockMvc.perform(delete(baseUri + "/picture")
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
    }

    @Test
    void deleteProfilePictureShouldWork_whenUserHasProfilePicture() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        mockMvc.perform(delete(baseUri + "/picture")
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
    }

    @Test
    void updatePassword_shouldUpdatePassword_whenValidOldPassword() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        String oldPassword = user.getUsername(); // testusers have the same username and password
        String newPassword = "newpass123";

        PasswordUpdateDTO dto = new PasswordUpdateDTO(oldPassword, newPassword);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(dto);

        mockMvc.perform(put(baseUri + "/update-password")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());
        keycloakClientService.validatePasswordOrThrow(userId, newPassword);

        // Restore previoous password for test consistency
        dto = new PasswordUpdateDTO(newPassword, oldPassword);
        body = mapper.writeValueAsString(dto);
        mockMvc.perform(put(baseUri + "/update-password")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());
        keycloakClientService.validatePasswordOrThrow(userId, oldPassword);
    }

    @Test
    void deleteProfile_shouldDeleteProfile() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        mockMvc.perform(delete(baseUri)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());

        User retrieved = entityManager.find(User.class, UUID.fromString(userId));
        assertThat(retrieved).isNull();
    }

    @Test
    void updateFcmToken_shouldUpdateFcmToken() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        FcmTokenDTO dto = new FcmTokenDTO("token");
        String body = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(put(baseUri + "/fcm-token")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());

        User retrieved = entityManager.find(User.class, UUID.fromString(userId));
        assertThat(retrieved.getFcmToken()).isEqualTo(dto.fcmToken());
    }

    @Test
    void updateNotificationPreferences_shouldUpdateNotificationPreferences() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        NotificationPreferencesDTO dto = new NotificationPreferencesDTO(false);
        String body = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(put(baseUri + "/preferences/notifications")
                .header("Content-Type", "application/json")
                .content(body)
                .with(jwt().jwt(jwt -> jwt.subject(userId))))
                .andExpect(status().isNoContent());

        User retrieved = entityManager.find(User.class, UUID.fromString(userId));
        assertThat(retrieved.getReceiveNotifications()).isFalse();
    }
}
