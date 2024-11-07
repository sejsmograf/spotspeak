package com.example.spotspeak.service;

import com.example.spotspeak.dto.FriendRequestDTO;
import com.example.spotspeak.dto.FriendRequestUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.exception.FriendRequestExistsException;
import com.example.spotspeak.exception.FriendRequestNotFoundException;
import com.example.spotspeak.exception.FriendshipExistsException;
import com.example.spotspeak.exception.InvalidFriendRequestStatusException;
import com.example.spotspeak.TestEntityFactory;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FriendRequestServiceIntegrationTest extends BaseServiceIntegrationTest {

    @Autowired
    private FriendRequestService friendRequestService;

    private User sender;
    private User receiver;

    @BeforeEach
    public void setUp() {
        sender = TestEntityFactory.createPersistedUser(entityManager);
        receiver = TestEntityFactory.createPersistedUser(entityManager);
    }

    @Nested
    class SendFriendRequestTests {
        @Test
        @Transactional
        void shouldCreatePendingRequest_whenValid() {
            FriendRequestDTO response = friendRequestService.sendFriendRequest(sender.getId().toString(),
                    receiver.getId());

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(EFriendRequestStatus.PENDING);
            assertThat(response.senderId()).isEqualTo(sender.getId());
            assertThat(response.receiverId()).isEqualTo(receiver.getId());
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestAlreadyReceivedFromUser() {
            friendRequestService.sendFriendRequest(receiver.getId().toString(), sender.getId());

            assertThrows(FriendRequestExistsException.class,
                    () -> friendRequestService.sendFriendRequest(sender.getId().toString(), receiver.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestAlreadySentToUser() {
            friendRequestService.sendFriendRequest(sender.getId().toString(), receiver.getId());

            assertThrows(FriendRequestExistsException.class,
                    () -> friendRequestService.sendFriendRequest(sender.getId().toString(), receiver.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenFriendshipAlreadyExists() {
            TestEntityFactory.createPersistedFriendship(entityManager, sender, receiver);

            assertThrows(FriendshipExistsException.class,
                    () -> friendRequestService.sendFriendRequest(sender.getId().toString(), receiver.getId()));
        }
    }

    @Nested
    class AcceptFriendRequestTests {

        @Test
        @Transactional
        void shouldAcceptRequest_whenValid() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender,
                    receiver, EFriendRequestStatus.PENDING);
            FriendRequestDTO response = friendRequestService.acceptFriendRequest(receiver.getId().toString(),
                    friendRequest.getId());

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(EFriendRequestStatus.ACCEPTED);
            assertThat(response.acceptedAt()).isNotNull();
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestNotPending() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender,
                    receiver, EFriendRequestStatus.ACCEPTED);

            assertThrows(InvalidFriendRequestStatusException.class,
                    () -> friendRequestService.acceptFriendRequest(receiver.getId().toString(), friendRequest.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenNotReceiver() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender,
                    receiver, EFriendRequestStatus.PENDING);

            assertThrows(ForbiddenException.class,
                    () -> friendRequestService.acceptFriendRequest(sender.getId().toString(), friendRequest.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestNotFound() {
            assertThrows(FriendRequestNotFoundException.class,
                    () -> friendRequestService.acceptFriendRequest(receiver.getId().toString(), 999L));
        }
    }

    @Nested
    class RejectFriendRequestTests {

        @Test
        @Transactional
        void shouldRejectRequest_whenValid() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender,
                    receiver, EFriendRequestStatus.PENDING);

            FriendRequestDTO response = friendRequestService.rejectFriendRequest(receiver.getId().toString(),
                    friendRequest.getId());

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(EFriendRequestStatus.REJECTED);
            assertThat(response.rejectedAt()).isNotNull();
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestNotPending() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender,
                    receiver, EFriendRequestStatus.REJECTED);

            assertThrows(InvalidFriendRequestStatusException.class,
                    () -> friendRequestService.rejectFriendRequest(receiver.getId().toString(), friendRequest.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenNotReceiver() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender,
                    receiver, EFriendRequestStatus.PENDING);

            assertThrows(ForbiddenException.class,
                    () -> friendRequestService.rejectFriendRequest(sender.getId().toString(), friendRequest.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestNotFound() {
            assertThrows(FriendRequestNotFoundException.class,
                    () -> friendRequestService.rejectFriendRequest(receiver.getId().toString(), 999L));
        }
    }

    @Nested
    class CancelFriendRequestTests {

        @Test
        @Transactional
        void shouldCancelRequest_whenUserIsSenderAndStatusIsPending() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(
                    entityManager, sender, receiver, EFriendRequestStatus.PENDING);

            friendRequestService.cancelFriendRequest(sender.getId().toString(), friendRequest.getId());
            flushAndClear();

            FriendRequest retrieved = entityManager.find(FriendRequest.class, friendRequest.getId());

            assertThat(retrieved).isNull();
        }

        @Test
        @Transactional
        void shouldThrowException_whenUserIsNotSender() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(
                    entityManager, sender, receiver, EFriendRequestStatus.PENDING);

            assertThrows(ForbiddenException.class,
                    () -> friendRequestService.cancelFriendRequest(receiver.getId().toString(), friendRequest.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestIsNotPending() {
            FriendRequest friendRequest = TestEntityFactory.createPersistedFriendRequest(
                    entityManager, sender, receiver, EFriendRequestStatus.ACCEPTED);

            assertThrows(InvalidFriendRequestStatusException.class,
                    () -> friendRequestService.cancelFriendRequest(sender.getId().toString(), friendRequest.getId()));
        }

        @Test
        @Transactional
        void shouldThrowException_whenRequestNotFound() {
            assertThrows(FriendRequestNotFoundException.class,
                    () -> friendRequestService.cancelFriendRequest(sender.getId().toString(), 999L));
        }
    }

    @Nested
    class GetSentFriendRequestsTests {

        @Test
        @Transactional
        void shouldReturnPendingSentRequests_whenExists() {
            User receiver1 = TestEntityFactory.createPersistedUser(entityManager);
            User receiver2 = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver1,
                    EFriendRequestStatus.PENDING);
            TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver2,
                    EFriendRequestStatus.PENDING);
            flushAndClear();

            List<FriendRequestUserInfoDTO> sentRequests = friendRequestService
                    .getSentFriendRequests(sender.getId().toString());

            assertThat(sentRequests).hasSize(2);
            assertThat(sentRequests).extracting("userInfo.id")
                    .containsExactlyInAnyOrder(receiver1.getId(), receiver2.getId());
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenNoPendingRequests() {
            List<FriendRequestUserInfoDTO> sentRequests = friendRequestService
                    .getSentFriendRequests(sender.getId().toString());

            assertThat(sentRequests).isEmpty();
        }
    }

    @Nested
    class GetReceivedFriendRequestsTests {

        @Test
        @Transactional
        void shouldReturnPendingReceivedRequests_whenExists() {
            User sender1 = TestEntityFactory.createPersistedUser(entityManager);
            User sender2 = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedFriendRequest(entityManager, sender1, receiver,
                    EFriendRequestStatus.PENDING);
            TestEntityFactory.createPersistedFriendRequest(entityManager, sender2, receiver,
                    EFriendRequestStatus.PENDING);
            flushAndClear();

            List<FriendRequestUserInfoDTO> receivedRequests = friendRequestService
                    .getReceivedFriendRequests(receiver.getId().toString());

            assertThat(receivedRequests).hasSize(2);
            assertThat(receivedRequests).extracting("userInfo.id")
                    .containsExactlyInAnyOrder(sender1.getId(), sender2.getId());
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenNoPendingRequests() {
            List<FriendRequestUserInfoDTO> receivedRequests = friendRequestService
                    .getReceivedFriendRequests(receiver.getId().toString());

            assertThat(receivedRequests).isEmpty();
        }
    }
}
