package com.example.spotspeak.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.spotspeak.controller.friendship.FriendshipController;
import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class FriendshipControllerTest extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FriendshipController friendshipController;

    private final URI baseUri = URI.create("/api/friends");

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
        assertThat(friendshipController).isNotNull();
    }

    @Test
    void getFriendsList_shouldReturnFriendshipUserInfoDTOList() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        User user = users.get(0);
        String userId = user.getId().toString();

        TestEntityFactory.createPersistedFriendship(entityManager, users.get(0), users.get(1));
        TestEntityFactory.createPersistedFriendship(entityManager, users.get(0), users.get(2));

        MockHttpServletResponse response = mockMvc.perform(get(baseUri)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        List<FriendshipUserInfoDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, FriendshipUserInfoDTO.class));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("friendInfo.id")
            .containsExactlyInAnyOrder(users.get(1).getId(), users.get(2).getId());
    }

    @Test
    void deleteFriend_shouldRemoveFriendship() throws Exception {
        User user = users.get(0);
        User friend = users.get(1);
        String userId = user.getId().toString();
        UUID friendId = friend.getId();

        FriendRequest friendship = TestEntityFactory.createPersistedFriendship(entityManager, user, friend);

        FriendRequest retrieved = entityManager.find(FriendRequest.class, friendship.getId());
        assertThat(retrieved).isNotNull();

        mockMvc.perform(delete(baseUri + "/" + friendId)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isNoContent());

        FriendRequest retrieved2 = entityManager.find(FriendRequest.class, friendship.getId());
        assertThat(retrieved2).isNull();
    }

    @Test
    void getMutualFriends_shouldReturnMutualFriendsList() throws Exception {
        User user = users.get(0);
        User targetUser = users.get(1);
        User mutualFriend = users.get(2);
        String userId = user.getId().toString();
        String targetUserId = targetUser.getId().toString();

        TestEntityFactory.createPersistedFriendship(entityManager, user, mutualFriend);
        TestEntityFactory.createPersistedFriendship(entityManager, targetUser, mutualFriend);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/mutual/" + targetUserId)
                .with(getMockAccessToken(userId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        List<AuthenticatedUserProfileDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, AuthenticatedUserProfileDTO.class));

        assertThat(result).hasSize(1);
        assertThat(result).extracting("id").containsExactly(mutualFriend.getId());
    }

    @Nested
    class ExceptionHandlerTests {
        @Test
        void deleteFriend_shouldReturnNotFoundWhenFriendshipNotExists() throws Exception {
            User user = users.get(0);
            User friend = users.get(1);
            String userId = user.getId().toString();
            UUID friendId = friend.getId();

            mockMvc.perform(delete(baseUri + "/" + friendId)
                    .with(getMockAccessToken(userId)))
                .andExpect(status().isNotFound());
        }

        @Test
        void deleteFriend_shouldReturnNotFoundWhenUserNotExists() throws Exception {
            User user = users.get(0);
            String userId = user.getId().toString();
            UUID friendId = UUID.randomUUID();

            mockMvc.perform(delete(baseUri + "/" + friendId)
                    .with(getMockAccessToken(userId)))
                .andExpect(status().isNotFound());
        }
    }
}
