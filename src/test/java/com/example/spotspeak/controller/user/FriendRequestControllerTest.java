package com.example.spotspeak.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.spotspeak.controller.friendship.FriendRequestController;
import com.example.spotspeak.dto.FriendRequestDTO;
import com.example.spotspeak.dto.FriendRequestUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

import jakarta.transaction.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class FriendRequestControllerTest extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FriendRequestController friendRequestController;

    private final URI baseUri = URI.create("/api/friend-requests");

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
        assertThat(friendRequestController).isNotNull();
    }

    @Test
    void sendFriendRequest_shouldReturnFriendRequestDTO() throws Exception {
        User sender = users.get(0);
        User receiver = users.get(1);
        String senderId = sender.getId().toString();
        UUID receiverId = receiver.getId();

        MockHttpServletResponse response = mockMvc.perform(post(baseUri + "/send/" + receiverId)
                .with(getMockAccessToken(senderId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        FriendRequestDTO result = mapper.readValue(response.getContentAsByteArray(), FriendRequestDTO.class);

        assertThat(result.senderId()).isEqualTo(sender.getId());
        assertThat(result.receiverId()).isEqualTo(receiver.getId());
    }

    @Test
    void acceptFriendRequest_shouldReturnUpdatedFriendRequestDTO() throws Exception {
        User sender = users.get(0);
        User receiver = users.get(1);
        String receiverId = receiver.getId().toString();

        FriendRequest request = TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

        MockHttpServletResponse response = mockMvc.perform(put(baseUri + "/accept/" + request.getId())
                .with(getMockAccessToken(receiverId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        FriendRequestDTO result = mapper.readValue(response.getContentAsByteArray(), FriendRequestDTO.class);

        assertThat(result.id()).isEqualTo(request.getId());
        assertThat(result.status()).isEqualTo(EFriendRequestStatus.ACCEPTED);
    }

    @Test
    void rejectFriendRequest_shouldReturnUpdatedFriendRequestDTO() throws Exception {
        User sender = users.get(0);
        User receiver = users.get(1);
        String receiverId = receiver.getId().toString();

        FriendRequest request = TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

        MockHttpServletResponse response = mockMvc.perform(put(baseUri + "/reject/" + request.getId())
                .with(getMockAccessToken(receiverId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        FriendRequestDTO result = mapper.readValue(response.getContentAsByteArray(), FriendRequestDTO.class);

        assertThat(result.id()).isEqualTo(request.getId());
        assertThat(result.status()).isEqualTo(EFriendRequestStatus.REJECTED);
    }

    @Test
    void cancelFriendRequest_shouldReturnNoContent() throws Exception {
        User sender = users.get(0);
        User receiver = users.get(1);
        String senderId = sender.getId().toString();

        FriendRequest request = TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

        mockMvc.perform(delete(baseUri + "/cancel/" + request.getId())
                .with(getMockAccessToken(senderId)))
            .andExpect(status().isNoContent());

        FriendRequest retrieved = entityManager.find(FriendRequest.class, request.getId());
        assertThat(retrieved).isNull();
    }

    @Test
    void getSentFriendRequests_shouldReturnFriendRequestUserInfoDTOList() throws Exception {
        User sender = users.get(0);
        User receiver = users.get(1);
        String senderId = sender.getId().toString();

        TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/sent")
                .with(getMockAccessToken(senderId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<FriendRequestUserInfoDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, FriendRequestUserInfoDTO.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userInfo().id()).isEqualTo(receiver.getId());
    }

    @Test
    void getReceivedFriendRequests_shouldReturnFriendRequestUserInfoDTOList() throws Exception {
        User sender = users.get(0);
        User receiver = users.get(1);
        String receiverId = receiver.getId().toString();

        TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/received")
                .with(getMockAccessToken(receiverId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<FriendRequestUserInfoDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, FriendRequestUserInfoDTO.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userInfo().id()).isEqualTo(sender.getId());
    }

    @Nested
    class ExceptionHandlerTests {
        @Test
        void sendFriendRequest_shouldReturnNotFoundWhenUserNotExists() throws Exception {
            User sender = users.get(0);
            String senderId = sender.getId().toString();
            UUID receiverId = UUID.randomUUID();

            mockMvc.perform(post(baseUri + "/send/" + receiverId)
                    .with(getMockAccessToken(senderId)))
                .andExpect(status().isNotFound());
        }

        @Test
        void sendFriendRequest_shouldReturnConflictWhenRequestAlreadyExists() throws Exception {
            User sender = users.get(0);
            User receiver = users.get(1);
            String senderId = sender.getId().toString();
            UUID receiverId = receiver.getId();

            TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

            mockMvc.perform(post(baseUri + "/send/" + receiverId)
                    .with(getMockAccessToken(senderId)))
                .andExpect(status().isConflict());
        }

        @Test
        void sendFriendRequest_shouldReturnConflictWhenFriendshipAlreadyExists() throws Exception {
            User sender = users.get(0);
            User receiver = users.get(1);
            String senderId = sender.getId().toString();
            UUID receiverId = receiver.getId();

            TestEntityFactory.createPersistedFriendship(entityManager, sender, receiver);

            mockMvc.perform(post(baseUri + "/send/" + receiverId)
                    .with(getMockAccessToken(senderId)))
                .andExpect(status().isConflict());
        }

        @Test
        void acceptFriendRequest_shouldReturnNotFoundWhenRequestNotExists() throws Exception {
            User user = users.get(0);
            String userId = user.getId().toString();
            long nonExistentRequestId = 999L;

            mockMvc.perform(put(baseUri + "/accept/" + nonExistentRequestId)
                    .with(getMockAccessToken(userId)))
                .andExpect(status().isNotFound());
        }

        @Test
        void acceptFriendRequest_shouldReturnConflictWhenInvalidRequestStatus() throws Exception {
            User sender = users.get(0);
            User receiver = users.get(1);
            String receiverId = receiver.getId().toString();

            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.REJECTED);

            mockMvc.perform(put(baseUri + "/accept/" + friendRequest.getId())
                    .with(getMockAccessToken(receiverId)))
                .andExpect(status().isConflict());
        }

        @Test
        void acceptFriendRequest_shouldReturnForbiddenWhenUserIsNotReceiver() throws Exception {
            User sender = users.get(0);
            User receiver = users.get(1);
            String senderId = sender.getId().toString();

            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);

            mockMvc.perform(put(baseUri + "/accept/" + friendRequest.getId())
                    .with(getMockAccessToken(senderId)))
                .andExpect(status().isForbidden());
        }
    }
}
