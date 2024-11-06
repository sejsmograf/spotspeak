package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.CommentMention;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

public class CommentMentionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CommentMentionRepository commentMentionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class BasicCommentMentionOperationsTests {

        @Test
        void saveCommentMention_shouldPersist() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            User mentionedUser = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            CommentMention mention = TestEntityFactory.createPersistedCommentMention(entityManager, comment,
                    mentionedUser);
            flushAndClear();

            assertThat(mention.getId()).isNotNull();
        }

        @Test
        void deleteCommentMention_shouldNotDeleteMentionedUserOrComment() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            User mentionedUser = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            CommentMention mention = TestEntityFactory.createPersistedCommentMention(entityManager, comment,
                    mentionedUser);
            flushAndClear();

            commentMentionRepository.deleteById(mention.getId());
            flushAndClear();

            assertThat(commentMentionRepository.findById(mention.getId())).isEmpty();
            assertThat(commentRepository.findById(comment.getId())).isPresent();
            assertThat(userRepository.findById(mentionedUser.getId())).isPresent();
        }

        @Test
        void deleteComment_shouldAlsoDeleteMentions() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            User mentionedUser = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            Comment comment = TestEntityFactory.createPersistedComment(entityManager, author, trace, "Test comment");
            CommentMention commentMention = TestEntityFactory.createPersistedCommentMention(entityManager, comment,
                    mentionedUser);
            flushAndClear();

            commentRepository.deleteById(comment.getId());
            flushAndClear();

            assertThat(commentMentionRepository.findById(commentMention.getId())).isEmpty();
            assertThat(commentMentionRepository.findByCommentId(comment.getId())).isEmpty();
        }
    }

}
