package com.example.spotspeak.controller.user;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.controller.ranking.RankingController;
import com.example.spotspeak.dto.RankingDTO;
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
public class RankingControllerTest extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RankingController rankingController;

    private final String baseUri = "/api/ranking";

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
        assertThat(rankingController).isNotNull();
    }

    @Test
    void getUserRanking_shouldReturnRankedListIncludingUserAndFriends() throws Exception {
        User mainUser = users.get(0);
        User friend1 = users.get(1);
        User friend2 = users.get(2);
        String mainUserId = mainUser.getId().toString();

        TestEntityFactory.createPersistedFriendship(entityManager, mainUser, friend1);
        TestEntityFactory.createPersistedFriendship(entityManager, mainUser, friend2);

        Achievement achievement1 = TestEntityFactory.createPersistedAchievement(
            entityManager,
            "First Trace",
            "Add your first trace",
            20,
            EEventType.ADD_TRACE,
            1,
            null
        );

        LocalDateTime localDateTime = LocalDateTime.of(2024, 11, 20, 10, 30, 10);

        TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null, localDateTime);
        TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement1, 1, 0, null, localDateTime);
        TestEntityFactory.createPersistedUserAchievement(entityManager, friend2, achievement1, 1, 0, null, localDateTime);

        flushAndClear();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri)
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper();
        List<RankingDTO> ranking = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, RankingDTO.class));

        assertThat(ranking).hasSize(3);
        assertThat(ranking).extracting("friendId")
            .containsExactlyInAnyOrder(users.get(0).getId(),users.get(1).getId(), users.get(2).getId());
    }
}
