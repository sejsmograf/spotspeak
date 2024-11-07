package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;

public class FriendshipRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = TestEntityFactory.createPersistedUser(entityManager);
        user2 = TestEntityFactory.createPersistedUser(entityManager);
    }

    @Nested
    class BasicFriendshipOperationsTests {

        @Test
        void saveFriendship_shouldPersist() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            flushAndClear();

            assertThat(friendship.getId()).isNotNull();
        }

        @Test
        void findFriendshipById_shouldReturnCorrectUsers() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            flushAndClear();

            Friendship found = friendshipRepository.findById(friendship.getId()).orElseThrow();

            assertThat(found).isEqualTo(friendship);
            assertThat(found.getUserInitiating()).isEqualTo(user1);
            assertThat(found.getUserReceiving()).isEqualTo(user2);
        }

        @Test
        void deleteFriendship_shouldNotDeleteUsers() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            flushAndClear();

            friendshipRepository.deleteById(friendship.getId());
            flushAndClear();

            assertThat(friendshipRepository.findById(friendship.getId())).isEmpty();
            assertThat(userRepository.findById(user1.getId())).isPresent();
            assertThat(userRepository.findById(user2.getId())).isPresent();
        }

        @Test
        void deleteUser_shouldAlsoDeleteFriendships() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            flushAndClear();

            userRepository.deleteById(user1.getId());
            flushAndClear();

            assertThat(friendshipRepository.findById(friendship.getId())).isEmpty();
            assertThat(friendshipRepository.findAllByUser(user2)).isEmpty();
        }
    }

    @Nested
    class FriendshipQueryTests {

        @Test
        void findAllByUser_shouldReturnAllFriendshipsOfUser() {
            User user3 = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            TestEntityFactory.createPersistedFriendship(entityManager, user1, user3);
            flushAndClear();

            List<Friendship> friendships = friendshipRepository.findAllByUser(user1);

            assertThat(friendships).hasSize(2);
        }

        @Test
        void findByUsers_shouldReturnFriendshipBetweenUsers() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            flushAndClear();

            Friendship found = friendshipRepository.findByUsers(user1, user2).orElseThrow();

            assertThat(found).isEqualTo(friendship);
        }

        @Test
        void findByUsers_shouldBeSymmetric() {
            TestEntityFactory.createPersistedFriendship(entityManager, user1, user2);
            flushAndClear();

            assertThat(friendshipRepository.findByUsers(user1, user2)).isPresent();
            assertThat(friendshipRepository.findByUsers(user2, user1)).isPresent();
        }

        @Test
        void findByUsers_shouldReturnEmpty_whenNoFriendshipExists() {
            assertThat(friendshipRepository.findByUsers(user1, user2)).isEmpty();
        }
    }
}
