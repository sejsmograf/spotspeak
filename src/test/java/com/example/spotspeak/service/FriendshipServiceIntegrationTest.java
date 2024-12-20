package com.example.spotspeak.service;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.entity.enumeration.ERelationStatus;
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
    class GetFriendsListTests {

        @Test
        @Transactional
        void shouldReturnFriendsList_whenUserHasFriends() {
            User friend2 = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            TestEntityFactory.createPersistedFriendship(entityManager, friend2, userInitiating);
            flushAndClear();

            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendshipDTOList(userInitiating.getId().toString());

            assertThat(friendsList).hasSize(2);
            assertThat(friendsList).extracting("friendInfo.id")
                    .containsExactlyInAnyOrder(userReceiving.getId(), friend2.getId());
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenUserHasNoFriends() {
            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendshipDTOList(userInitiating.getId().toString());

            assertThat(friendsList).isEmpty();
        }

        @Test
        @Transactional
        void shouldReturnSingleFriend_whenUserIsOnlyInitiator() {
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            flushAndClear();

            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendshipDTOList(userInitiating.getId().toString());

            assertThat(friendsList).hasSize(1);
            assertThat(friendsList.get(0).friendInfo().id()).isEqualTo(userReceiving.getId());
        }

        @Test
        @Transactional
        void shouldReturnSingleFriend_whenUserIsOnlyReceiver() {
            TestEntityFactory.createPersistedFriendship(entityManager, userReceiving, userInitiating);
            flushAndClear();

            List<FriendshipUserInfoDTO> friendsList = friendshipService
                    .getFriendshipDTOList(userInitiating.getId().toString());

            assertThat(friendsList).hasSize(1);
            assertThat(friendsList.get(0).friendInfo().id()).isEqualTo(userReceiving.getId());
        }
    }

    @Nested
    class DeleteFriendTests {

        @Test
        @Transactional
        void shouldDeleteFriendship_whenExists() {
            FriendRequest friendship = TestEntityFactory.createPersistedFriendship(entityManager, userInitiating,
                    userReceiving);
            flushAndClear();

            friendshipService.deleteFriend(userInitiating.getId().toString(), userReceiving.getId());
            flushAndClear();

            FriendRequest retrieved = entityManager.find(FriendRequest.class, friendship.getId());

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
            FriendRequest friendship = TestEntityFactory.createPersistedFriendship(entityManager, userReceiving,
                    userInitiating);
            flushAndClear();

            friendshipService.deleteFriend(userInitiating.getId().toString(), userReceiving.getId());
            flushAndClear();

            FriendRequest retrieved = entityManager.find(FriendRequest.class, friendship.getId());

            assertThat(retrieved).isNull();
        }
    }

    @Nested
    class getFriendshipStatus {
        @Test
        @Transactional
        void shouldReturnFriendsStatus_whenUsersAreFriends() {
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            flushAndClear();

            ERelationStatus friendshipStatus = friendshipService.getFriendshipStatus(String.valueOf(userInitiating.getId()), String.valueOf(userReceiving.getId()));

            assertThat(friendshipStatus).isEqualTo(ERelationStatus.FRIENDS);
        }

        @Test
        @Transactional
        void shouldReturnInvitationSentStatus_whenUserSentInvitation() {
            TestEntityFactory.createPersistedFriendRequest(entityManager, userInitiating, userReceiving, EFriendRequestStatus.PENDING);
            flushAndClear();

            ERelationStatus friendshipStatus = friendshipService.getFriendshipStatus(String.valueOf(userInitiating.getId()), String.valueOf(userReceiving.getId()));

            assertThat(friendshipStatus).isEqualTo(ERelationStatus.INVITATION_SENT);
        }

        @Test
        @Transactional
        void shouldReturnInvitationReceivedStatus_whenUserReceivedInvitation() {
            TestEntityFactory.createPersistedFriendRequest(entityManager, userReceiving, userInitiating, EFriendRequestStatus.PENDING);
            flushAndClear();

            ERelationStatus friendshipStatus = friendshipService.getFriendshipStatus(String.valueOf(userInitiating.getId()), String.valueOf(userReceiving.getId()));

            assertThat(friendshipStatus).isEqualTo(ERelationStatus.INVITATION_RECEIVED);
        }

        @Test
        @Transactional
        void shouldReturnNoRelationStatus_whenUsersHaveNoRelationship() {
            ERelationStatus friendshipStatus = friendshipService.getFriendshipStatus(String.valueOf(userInitiating.getId()), String.valueOf(userReceiving.getId()));

            assertThat(friendshipStatus).isEqualTo(ERelationStatus.NO_RELATION);
        }
    }

    @Nested
    class getMutualFriends {

        @Test
        @Transactional
        void shouldReturnEmptyList_whenNoMutualFriendsExists() {
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            flushAndClear();

            List<AuthenticatedUserProfileDTO> mutualFriendsDTOs = friendshipService.getMutualFriendsDTO(String.valueOf(userInitiating.getId()), String.valueOf(userReceiving.getId()));
            assertThat(mutualFriendsDTOs).isEmpty();
        }

        @Test
        @Transactional
        void shouldReturnMutualFriendsList_whenMutualFriendsExists() {
            User mutualFriend1 = TestEntityFactory.createPersistedUser(entityManager);
            User mutualFriend2 = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, userReceiving);
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, mutualFriend1);
            TestEntityFactory.createPersistedFriendship(entityManager, userReceiving, mutualFriend1);
            TestEntityFactory.createPersistedFriendship(entityManager, userInitiating, mutualFriend2);
            TestEntityFactory.createPersistedFriendship(entityManager, userReceiving, mutualFriend2);
            flushAndClear();

            List<AuthenticatedUserProfileDTO> mutualFriendsDTOs = friendshipService.getMutualFriendsDTO(String.valueOf(userInitiating.getId()), String.valueOf(userReceiving.getId()));
            assertThat(mutualFriendsDTOs).hasSize(2);
            assertThat(mutualFriendsDTOs).extracting("id")
                .containsExactlyInAnyOrder(mutualFriend1.getId(), mutualFriend2.getId());
        }
    }
}
