package com.example.spotspeak.controller.user;

import com.example.spotspeak.BaseTestWithKeycloak;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.controller.comment.CommentController;
import com.example.spotspeak.dto.CommentRequestDTO;
import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest extends BaseTestWithKeycloak {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentController commentController;

    private final String baseUri = "/api/traces/comments";

    private List<User> users;
    private Trace trace;

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

        trace = TestEntityFactory.createPersistedTrace(entityManager, users.get(0), null);
        flushAndClear();
    }

    @Test
    void contextLoads() {
        assertThat(commentController).isNotNull();
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();

        CommentRequestDTO commentRequest = TestEntityFactory.createCommentRequestDTO("This is a test comment", null);
        flushAndClear();

        MockHttpServletResponse response = mockMvc.perform(post(baseUri + "/" + trace.getId())
                .with(getMockAccessToken(mainUserId))
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(commentRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());;
        CommentResponseDTO result = mapper.readValue(response.getContentAsByteArray(), CommentResponseDTO.class);

        assertThat(result.content()).isEqualTo("This is a test comment");
        assertThat(result.author().id()).isEqualTo(mainUser.getId());
    }

    @Test
    void getTraceComments_shouldReturnListOfComments() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();

        TestEntityFactory.createPersistedComment(entityManager, mainUser, trace, "First comment");
        TestEntityFactory.createPersistedComment(entityManager, mainUser, trace, "Second comment");
        flushAndClear();

        MockHttpServletResponse response = mockMvc.perform(get(baseUri + "/" + trace.getId())
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());;
        List<CommentResponseDTO> result = mapper.readValue(response.getContentAsByteArray(),
            mapper.getTypeFactory().constructCollectionType(List.class, CommentResponseDTO.class));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("content").containsExactlyInAnyOrder("First comment", "Second comment");
    }

    @Test
    void updateComment_shouldReturnUpdatedComment() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();

        Comment comment = TestEntityFactory.createPersistedComment(entityManager, mainUser, trace, "Old comment");
        flushAndClear();

        CommentRequestDTO updateRequest = TestEntityFactory.createCommentRequestDTO("Updated comment", null);
        flushAndClear();

        MockHttpServletResponse response = mockMvc.perform(put(baseUri + "/" + comment.getId())
                .with(getMockAccessToken(mainUserId))
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());;
        CommentResponseDTO result = mapper.readValue(response.getContentAsByteArray(), CommentResponseDTO.class);

        assertThat(result.content()).isEqualTo("Updated comment");
    }

    @Test
    void deleteComment_shouldReturnNoContent() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();

        Comment comment = TestEntityFactory.createPersistedComment(entityManager, mainUser, trace, "Comment to delete");
        flushAndClear();

        mockMvc.perform(delete(baseUri + "/" + comment.getId())
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isNoContent());

        Comment deletedComment = entityManager.find(comment.getClass(), comment.getId());
        assertThat(deletedComment).isNull();
    }

    @Test
    void addComment_shouldReturnNotFoundWhenTraceNotExists() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();
        long nonExistentId = 999L;
        CommentRequestDTO commentRequest = TestEntityFactory.createCommentRequestDTO("This is a test comment", null);
        flushAndClear();

        mockMvc.perform(post(baseUri + "/" + nonExistentId)
                .with(getMockAccessToken(mainUserId))
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(commentRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_shouldReturnNotFoundWhenCommentNotExists() throws Exception {
        User mainUser = users.get(0);
        String mainUserId = mainUser.getId().toString();
        long nonExistentId = 999L;

        mockMvc.perform(delete(baseUri + "/" + nonExistentId)
                .with(getMockAccessToken(mainUserId)))
            .andExpect(status().isNotFound());
    }
}
