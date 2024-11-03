package com.example.spotspeak.repository;

import com.example.spotspeak.entity.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TraceRepository traceRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class BasicCommentOperationsTests {

        @Test
        void saveComment_shouldPersist() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            flushAndClear();

            assertThat(comment.getId()).isNotNull();
        }

        @Test
        void findCommentById_shouldReturnCorrectComment() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            flushAndClear();

            Comment found = commentRepository.findById(comment.getId()).orElseThrow();

            assertThat(found.getContent()).isEqualTo("Test comment");
            assertThat(found.getAuthor()).isEqualTo(author);
            assertThat(found.getTrace()).isEqualTo(trace);
        }

        @Test
        void deleteComment_shouldNotDeleteTraceOrAuthor() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            flushAndClear();

            commentRepository.deleteById(comment.getId());
            flushAndClear();

            assertThat(commentRepository.findById(comment.getId())).isEmpty();
            assertThat(traceRepository.findById(trace.getId())).isPresent();
            assertThat(userRepository.findById(author.getId())).isPresent();
        }

        @Test
        void deleteTrace_shouldAlsoDeleteComments() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            flushAndClear();

            traceRepository.deleteById(trace.getId());
            flushAndClear();

            assertThat(traceRepository.findById(trace.getId())).isEmpty();
            assertThat(commentRepository.findById(comment.getId())).isEmpty();
        }

        @Test
        void findCommentById_shouldReturnCorrectMentions() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            User mentionedUser1 = TestEntityFactory.createPersistedUser(entityManager);
            User mentionedUser2 = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);

            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Comment with mentions");

            CommentMention mention1 = TestEntityFactory.createPersistedCommentMention(entityManager, comment, mentionedUser1);
            CommentMention mention2 = TestEntityFactory.createPersistedCommentMention(entityManager, comment, mentionedUser2);

            comment.setMentions(List.of(mention1, mention2));
            entityManager.persist(comment);
            flushAndClear();

            Comment foundComment = commentRepository.findById(comment.getId()).orElseThrow();
            List<CommentMention> mentions = foundComment.getMentions();

            assertThat(mentions).isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrderElementsOf(List.of(mention1, mention2));
        }
    }

    @Nested
    class CommentQueryTests {

        @Test
        void findCommentsByTrace_shouldReturnAllCommentsForTrace() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            TestEntityFactory.createPersistedComment(entityManager, author, trace, "First comment");
            TestEntityFactory.createPersistedComment(entityManager, author, trace, "Second comment");
            flushAndClear();

            List<Comment> comments = commentRepository.findByTraceId(trace.getId());

            assertThat(comments).hasSize(2);
        }
    }
}
