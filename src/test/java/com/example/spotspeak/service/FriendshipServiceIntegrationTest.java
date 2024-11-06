package com.example.spotspeak.service;

import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.FriendshipNotFoundException;
import com.example.spotspeak.TestEntityFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FriendshipServiceIntegrationTest extends BaseServiceIntegrationTest {

    @Autowired
    private FriendshipService friendshipService;

    private User userInitiating;
    private User userReceiving;

    @BeforeEach
    public void setUp() {
        userInitiating = TestEntityFactory.createPersistedUser(entityManager);
        userReceiving = TestEntityFactory.createPersistedUser(entityManager);
    }

    @Nested
    class CreateFriendshipTests {

        @Test
        @Transactional
        void shouldCreateFriendship_whenNotExists() {
            Friendship friendship = friendshipService.createFriendship(userInitiating, userReceiving);
            flushAndClear();

            Friendship retrieved = entityManager.find(Friendship.class, friendship.getId());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUserInitiating()).isEqualTo(userInitiating);
            assertThat(retrieved.getUserReceiving()).isEqualTo(userReceiving);
        }

        @Test
        @Transactional
        void shouldThrowException_whenFriendshipAlreadyExists() {
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            flushAndClear();

            assertThrows(FriendshipNotFoundException.class,
                    () -> friendshipService.createFriendship(userInitiating, userReceiving));
        }
    }

    @Nested
    class GetFriendsListTests {

        @Test
        @Transactional
        void shouldReturnFriendsList_whenUserHasFriends() {
            User friend2 = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            TestEntityFactory.createPersistedFriendship(entityManager, friend2, userInitiating);
            flushAndClear();

            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendsList(userInitiating.getId().toString());

            assertThat(friendsList).hasSize(2);
            assertThat(friendsList).extracting("friendInfo.id")
                    .containsExactlyInAnyOrder(userReceiving.getId(), friend2.getId());
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenUserHasNoFriends() {
            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendsList(userInitiating.getId().toString());

            assertThat(friendsList).isEmpty();
        }

        @Test
        @Transactional
        void shouldReturnSingleFriend_whenUserIsOnlyInitiator() {
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            flushAndClear();

            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendsList(userInitiating.getId().toString());

            assertThat(friendsList).hasSize(1);
            assertThat(friendsList.get(0).friendInfo().id()).isEqualTo(userReceiving.getId());
        }

        @Test
        @Transactional
        void shouldReturnSingleFriend_whenUserIsOnlyReceiver() {
            TestEntityFactory.createPersistedFriendship(entityManager, userReceiving, userInitiating);
            flushAndClear();

            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendsList(userInitiating.getId().toString());

            assertThat(friendsList).hasSize(1);
            assertThat(friendsList.get(0).friendInfo().id()).isEqualTo(userReceiving.getId());
        }
    }

    @Nested
    class DeleteFriendTests {

        @Test
        @Transactional
        void shouldDeleteFriendship_whenExists() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, userInitiating,
                    userReceiving);
            flushAndClear();

            friendshipService.deleteFriend(userInitiating.getId().toString(), userReceiving.getId());
            flushAndClear();

            Friendship retrieved = entityManager.find(Friendship.class, friendship.getId());

            assertThat(retrieved).isNull();
        }

        @Test
        @Transactional
        void shouldThrowException_whenFriendshipDoesNotExist() {
            assertThrows(FriendshipNotFoundException.class,
                    () -> friendshipService.deleteFriend(userInitiating.getId().toString(), userReceiving.getId()));
        }

        @Test
        @Transactional
        void shouldDeleteFriendship_whenUserIsReceiver() {
            Friendship friendship = TestEntityFactory.createPersistedFriendship(entityManager, userReceiving,
                    userInitiating);
            flushAndClear();

            friendshipService.deleteFriend(userInitiating.getId().toString(), userReceiving.getId());
            flushAndClear();

            Friendship retrieved = entityManager.find(Friendship.class, friendship.getId());

            assertThat(retrieved).isNull();
        }
    }

    @Nested
    class CheckFriendshipExistsTests {

        @Test
        @Transactional
        void shouldReturnTrue_whenFriendshipExists() {
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            flushAndClear();

            boolean exists = friendshipService.checkFriendshipExists(userInitiating, userReceiving);

            assertThat(exists).isTrue();
        }

        @Test
        @Transactional
        void shouldReturnFalse_whenFriendshipDoesNotExist() {
            boolean exists = friendshipService.checkFriendshipExists(userInitiating, userReceiving);

            assertThat(exists).isFalse();
        }

        @Test
        @Transactional
        void shouldReturnTrue_whenUsersAreTheSame() {
            boolean exists = friendshipService.checkFriendshipExists(userInitiating, userInitiating);

            assertThat(exists).isTrue();
        }
    }
}
