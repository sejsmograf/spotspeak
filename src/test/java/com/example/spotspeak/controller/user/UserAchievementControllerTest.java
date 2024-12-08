package com.example.spotspeak.controller.user;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.controller.achievement.UserAchievementController;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDetailsDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserAchievementControllerTest extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAchievementController userAchievementController;

    private final String baseUri = "/api/achievements";

    private List<User> users;

    private Achievement achievement;
    private UserAchievement userAchievement;
    private LocalDateTime localDateTime;

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

        achievement = TestEntityFactory.createPersistedAchievement(
                entityManager,
                "First Trace",
                "Add your first trace",
                20,
                EEventType.ADD_TRACE,
                1,
                null
        );
        localDateTime = LocalDateTime.now();
        userAchievement = TestEntityFactory.createPersistedUserAchievement(entityManager, users.get(0), achievement, 1, 0, null, localDateTime);
        flushAndClear();
    }

    @Test
    void contextLoads() {
        assertThat(userAchievementController).isNotNull();
    }

    @Test
    void getUserAchievements_shouldReturnUserAchievements() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/my")
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<UserAchievementDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, UserAchievementDTO.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).achievementName()).isEqualTo("First Trace");
    }

    @Test
    void getUserAchievementDetails_shouldReturnAchievementDetails() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/details/" + userAchievement.getId())
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        UserAchievementDetailsDTO result = mapper.readValue(response.getContentAsByteArray(), UserAchievementDetailsDTO.class);

        assertThat(result.achievementName()).isEqualTo("First Trace");
        assertThat(result.achievementDescription()).isEqualTo("Add your first trace");
    }

    @Test
    void getFriendsWhoCompletedAchievement_shouldReturnFriendList() throws Exception {
        User mainUser = users.get(0);
        User friend = users.get(1);
        String mainUserId = mainUser.getId().toString();

        TestEntityFactory.createPersistedFriendship(entityManager, mainUser, friend);
        TestEntityFactory.createPersistedUserAchievement(entityManager, friend, achievement, 1, 0, null, localDateTime);
        flushAndClear();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/details/" + userAchievement.getId() + "/friends")
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<PublicUserProfileDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, PublicUserProfileDTO.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo(friend.getUsername());
    }

    @Test
    void getCompletedAchievementsByUser_shouldReturnCompletedAchievementsForUser() throws Exception {
        User mainUser = users.get(0);
        User otherUser = users.get(1);
        String mainUserId = mainUser.getId().toString();

        TestEntityFactory.createPersistedUserAchievement(entityManager, otherUser, achievement, 1, 0, null, localDateTime);
        flushAndClear();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/" + otherUser.getId())
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<UserAchievementDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, UserAchievementDTO.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).achievementName()).isEqualTo("First Trace");
    }
}
