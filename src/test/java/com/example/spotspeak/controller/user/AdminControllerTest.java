package com.example.spotspeak.controller.user;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.controller.admin.AdminController;
import com.example.spotspeak.dto.achievement.AchievementResponseDTO;
import com.example.spotspeak.dto.achievement.AchievementUpdateDTO;
import com.example.spotspeak.dto.achievement.AchievementUploadDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTest extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminController adminController;

    private final String baseUri = "/api/admin";

    private List<User> users;
    private Achievement achievement;

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

        achievement = TestEntityFactory.createPersistedAchievement(entityManager, "First Achievement", "Description", 10, EEventType.ADD_TRACE, 1, null);
        TestEntityFactory.createPersistedAchievement(entityManager, "Second Achievement", "Description", 20, EEventType.ADD_TRACE, 1, null);
        flushAndClear();
    }

    @Test
    void contextLoads() {
        assertThat(adminController).isNotNull();
    }

    @Test
    void getAchievements_shouldReturnAchievementList() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/achievements")
                .with(getMockAccessToken(userId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper();
        List<AchievementResponseDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, AchievementResponseDTO.class));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").contains("First Achievement", "Second Achievement");
    }

    @Test
    void createAchievement_shouldReturnCreatedAchievement() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        AchievementUploadDTO achievementUploadDTO = TestEntityFactory.createAchievementUploadDTO(
            "New Achievement",
            "Test Description",
            30,
            "ADD_TRACE",
            1,
            List.of()
        );

        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 100);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "achievementUploadDTO",
            "achievementUploadDTO",
            "application/json",
            new ObjectMapper().writeValueAsBytes(achievementUploadDTO)
        );

        MockHttpServletResponse response = mockMvc.perform(multipart(baseUri + "/create-achievement")
                .file(file)
                .file(jsonPart)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper();
        AchievementResponseDTO result = mapper.readValue(response.getContentAsByteArray(), AchievementResponseDTO.class);

        assertThat(result.name()).isEqualTo("New Achievement");
    }

    @Test
    void updateAchievement_shouldReturnUpdatedAchievement() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        AchievementUpdateDTO achievementUpdateDTO = TestEntityFactory.createAchievementUpdateDTO(
            achievement.getId(),
            "Updated Achievement",
            "Updated Description",
            50,
            "DISCOVER_TRACE",
            2,
            List.of()
        );

        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 100);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "achievementUpdateDTO",
            "achievementUpdateDTO",
            "application/json",
            new ObjectMapper().writeValueAsBytes(achievementUpdateDTO)
        );

        MockHttpServletResponse response = mockMvc.perform(multipart(baseUri + "/update-achievement")
                .file(file)
                .file(jsonPart)
                .with(getMockAccessToken(userId))
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper();
        AchievementResponseDTO result = mapper.readValue(response.getContentAsByteArray(), AchievementResponseDTO.class);

        assertThat(result.name()).isEqualTo("Updated Achievement");
        assertThat(result.points()).isEqualTo(50);
    }

    @Test
    void deleteAchievement_shouldRemoveAchievement() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        mockMvc.perform(delete(baseUri + "/delete-achievement/" + achievement.getId())
                .with(getMockAccessToken(userId)))
            .andExpect(status().isNoContent());

        Achievement deletedAchievement = entityManager.find(Achievement.class, achievement.getId());
        assertThat(deletedAchievement).isNull();
    }

    @Test
    void createAchievement_shouldReturnConflictWhenAchievementExists() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        AchievementUploadDTO achievementUploadDTO = TestEntityFactory.createAchievementUploadDTO(
            achievement.getName(),
            "Test Description",
            30,
            "ADD_TRACE",
            1,
            List.of()
        );

        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 100);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "achievementUploadDTO",
            "achievementUploadDTO",
            "application/json",
            new ObjectMapper().writeValueAsBytes(achievementUploadDTO)
        );

        mockMvc.perform(multipart(baseUri + "/create-achievement")
                .file(file)
                .file(jsonPart)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isConflict());
    }

    @Test
    void createAchievement_shouldReturnBadRequestWhenInvalidEventType() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();

        AchievementUploadDTO achievementUploadDTO = TestEntityFactory.createAchievementUploadDTO(
            "New achievement",
            "Test Description",
            30,
            "INVALID_TYPE",
            1,
            List.of()
        );

        MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 100);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "achievementUploadDTO",
            "achievementUploadDTO",
            "application/json",
            new ObjectMapper().writeValueAsBytes(achievementUploadDTO)
        );

        mockMvc.perform(multipart(baseUri + "/create-achievement")
                .file(file)
                .file(jsonPart)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAchievement_shouldReturnNotFoundWhenAchievementNotExists() throws Exception {
        User user = users.get(0);
        String userId = user.getId().toString();
        long nonExistentId = 999L;

        mockMvc.perform(delete(baseUri + "/delete-achievement/" + nonExistentId)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isNotFound());
    }
}
