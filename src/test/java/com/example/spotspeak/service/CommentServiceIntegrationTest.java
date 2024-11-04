package com.example.spotspeak.service;

import com.example.spotspeak.dto.CommentRequestDTO;
import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.CommentMention;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.CommentNotFoundException;
import com.example.spotspeak.repository.TestEntityFactory;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class CommentServiceIntegrationTest extends BaseServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    private User testUser;
    private User mentionedUser;
    private Trace testTrace;

    @BeforeEach
    public void setUp() {
        testUser = TestEntityFactory.createPersistedUser(entityManager);
        testTrace = TestEntityFactory.createPersistedTrace(entityManager, testUser, null);
        mentionedUser = TestEntityFactory.createPersistedUser(entityManager);
        mentionedUser.setUsername("user123");
        entityManager.persist(mentionedUser);
    }

    @Nested
    class AddCommentTest {
        @Test
        @Transactional
        void shouldAddCommentToTrace_whenValidRequest() {
            CommentRequestDTO commentRequest = TestEntityFactory.createCommentRequestDTO("Test comment");

            CommentResponseDTO responseDTO = commentService.addComment(testUser.getId().toString(), testTrace.getId(), commentRequest);

            assertThat(responseDTO).isNotNull();
            assertThat(responseDTO.content()).isEqualTo(commentRequest.content());
            assertThat(responseDTO.author().id()).isEqualTo(testUser.getId());

            Comment savedComment = entityManager.find(Comment.class, responseDTO.id());

            assertThat(savedComment.getContent()).isEqualTo(responseDTO.content());
            assertThat(savedComment.getTrace()).isEqualTo(testTrace);
            assertThat(savedComment.getAuthor()).isEqualTo(testUser);
            assertThat(savedComment.getMentions()).isEmpty();
        }

        @Test
        @Transactional
        void shouldProcessMentions_whenRequestContainsMentions() {
            CommentRequestDTO commentRequest = TestEntityFactory.createCommentRequestDTO("Mentioning someone @user123");

            CommentResponseDTO responseDTO = commentService.addComment(testUser.getId().toString(), testTrace.getId(), commentRequest);

            Comment savedComment = entityManager.find(Comment.class, responseDTO.id());
            assertThat(savedComment.getMentions()).isNotEmpty();
            assertThat(savedComment.getMentions())
                .hasSize(1)
                .extracting(CommentMention::getMentionedUser)
                .containsExactly(mentionedUser);
        }
    }

    @Nested
    class GetTraceCommentsTests {

        @Test
        @Transactional
        void shouldReturnComments_whenCommentsExistForTrace() {
            Comment comment1 = TestEntityFactory.createPersistedComment(entityManager, testUser, testTrace, "First comment");
            Comment comment2 = TestEntityFactory.createPersistedComment(entityManager, testUser, testTrace, "Second comment");
            flushAndClear();

            List<CommentResponseDTO> comments = commentService.getTraceComments(testUser.getId().toString(), testTrace.getId());

            assertThat(comments).hasSize(2);
            assertThat(comments).extracting("id").containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
            assertThat(comments).extracting("content").containsExactlyInAnyOrder("First comment", "Second comment");
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenNoCommentsExistForTrace() {
            List<CommentResponseDTO> comments = commentService.getTraceComments(testUser.getId().toString(), testTrace.getId());

            assertThat(comments).isEmpty();
        }
    }

    @Nested
    class UpdateCommentTests {

        @Test
        @Transactional
        void shouldUpdateCommentContent_whenAuthorUpdates() {
            Comment testComment = TestEntityFactory.createPersistedComment(entityManager, testUser, testTrace, "Original content");
            CommentRequestDTO updateRequest = TestEntityFactory.createCommentRequestDTO("Updated content");
            flushAndClear();

            CommentResponseDTO response = commentService.updateComment(testUser.getId().toString(), testComment.getId(), updateRequest);

            Comment updatedComment = entityManager.find(Comment.class, response.id());

            assertThat(updatedComment.getId()).isEqualTo(testComment.getId());
            assertThat(updatedComment.getAuthor()).isEqualTo(testUser);
            assertThat(updatedComment.getContent()).isEqualTo("Updated content");

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(testComment.getId());
            assertThat(response.author().id()).isEqualTo(testUser.getId());
            assertThat(response.content()).isEqualTo("Updated content");
        }

        @Test
        @Transactional
        void shouldUpdateCommentContent_whenAuthorUpdates_whenRequestContainsMentions() {
            CommentRequestDTO commentRequest = TestEntityFactory.createCommentRequestDTO("Original. Mentioning someone @user123");
            CommentResponseDTO responseDTO = commentService.addComment(testUser.getId().toString(), testTrace.getId(), commentRequest);
            Comment comment = entityManager.find(Comment.class, responseDTO.id());

            User updateMentionedUser = TestEntityFactory.createPersistedUser(entityManager);
            updateMentionedUser.setUsername("user456");
            entityManager.persist(updateMentionedUser);

            CommentRequestDTO updateRequest = TestEntityFactory.createCommentRequestDTO("Updated. Mentioning someone @user456");
            CommentResponseDTO response = commentService.updateComment(testUser.getId().toString(), comment.getId(), updateRequest);

            Comment updatedComment = entityManager.find(Comment.class, response.id());
            assertThat(updatedComment.getContent()).isEqualTo("Updated. Mentioning someone @user456");
            assertThat(updatedComment.getMentions()).isNotEmpty();
            assertThat(updatedComment.getMentions())
                .hasSize(1)
                .extracting(CommentMention::getMentionedUser)
                .containsExactly(updateMentionedUser);

            assertThat(response).isNotNull();
            assertThat(response.content()).isEqualTo("Updated. Mentioning someone @user456");
        }

        @Test
        @Transactional
        void shouldThrowException_whenNonAuthorTriesToUpdate() {
            Comment testComment = TestEntityFactory.createPersistedComment(entityManager, testUser, testTrace, "Test comment");
            User otherUser = TestEntityFactory.createPersistedUser(entityManager);
            CommentRequestDTO updateRequest = new CommentRequestDTO("Test comment");

            assertThrows(ForbiddenException.class, () ->
                commentService.updateComment(otherUser.getId().toString(), testComment.getId(), updateRequest));
        }

        @Test
        @Transactional
        void shouldThrowException_whenCommentNotFound() {
            CommentRequestDTO updateRequest = new CommentRequestDTO("Test comment");

            assertThrows(CommentNotFoundException.class, () ->
                commentService.updateComment(testUser.getId().toString(), 999L, updateRequest));
        }
    }

    @Nested
    class DeleteCommentTests {

        @Test
        @Transactional
        void shouldDeleteComment_whenAuthorDeletes() {
            Comment testComment = TestEntityFactory.createPersistedComment(entityManager, testUser, testTrace, "Test comment");
            commentService.deleteComment(testUser.getId().toString(), testComment.getId());

            assertThat(entityManager.find(Comment.class, testComment.getId())).isNull();
        }

        @Test
        @Transactional
        void shouldThrowException_whenNonAuthorTriesToDelete() {
            Comment testComment = TestEntityFactory.createPersistedComment(entityManager, testUser, testTrace, "Test comment");
            User otherUser = TestEntityFactory.createPersistedUser(entityManager);

            assertThrows(ForbiddenException.class, () ->
                commentService.deleteComment(otherUser.getId().toString(), testComment.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenCommentNotFound() {
            assertThrows(CommentNotFoundException.class, () ->
                commentService.deleteComment(testUser.getId().toString(), 999L));
        }
    }

}
