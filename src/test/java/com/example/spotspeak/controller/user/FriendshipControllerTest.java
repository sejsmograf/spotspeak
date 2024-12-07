package com.example.spotspeak.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.example.spotspeak.controller.friendship.FriendshipController;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
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

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.KeycloakClientService;
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

    @Autowired
    KeycloakClientService keycloakClientService;

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
}
