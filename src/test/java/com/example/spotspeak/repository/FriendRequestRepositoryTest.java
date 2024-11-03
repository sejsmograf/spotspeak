package com.example.spotspeak.repository;

import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FriendRequestRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User sender;
    private User receiver;
    private FriendRequest friendRequest;

    @BeforeEach
    void setUp() {
        sender = TestEntityFactory.createPersistedUser(entityManager);
        receiver = TestEntityFactory.createPersistedUser(entityManager);
        friendRequest = TestEntityFactory.createPersistedFriendRequest(entityManager, sender, receiver, EFriendRequestStatus.PENDING);
        flushAndClear();
    }

    @Nested
    class BasicFriendRequestOperationsTests {

        @Test
        void saveFriendRequest_shouldPersist() {
            assertThat(friendRequest.getId()).isNotNull();
        }

        @Test
        void findFriendRequestById_shouldReturnCorrectUsers() {
            FriendRequest found = friendRequestRepository.findById(friendRequest.getId()).orElseThrow();

            assertThat(found).isEqualTo(friendRequest);
            assertThat(found.getSender()).isEqualTo(sender);
            assertThat(found.getReceiver()).isEqualTo(receiver);
        }

        @Test
        void deleteFriendRequest_shouldNotDeleteUsers() {
            friendRequestRepository.deleteById(friendRequest.getId());
            flushAndClear();

            assertThat(friendRequestRepository.findById(friendRequest.getId())).isEmpty();
            assertThat(userRepository.findById(sender.getId())).isPresent();
            assertThat(userRepository.findById(receiver.getId())).isPresent();
        }

        @Test
        void deleteUser_shouldAlsoDeleteFriendRequests() {
            userRepository.deleteById(sender.getId());
            flushAndClear();

            assertThat(friendRequestRepository.findById(friendRequest.getId())).isEmpty();
            assertThat(friendRequestRepository.findAllByUser(receiver)).isEmpty();
        }
    }

    @Nested
    class FriendRequestQueryTests {

        @Test
        void existsBySenderAndReceiverAndStatus_shouldReturnTrue_whenRequestExists() {
            boolean exists = friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, EFriendRequestStatus.PENDING);

            assertThat(exists).isTrue();
        }

        @Test
        void findBySenderAndStatus_shouldReturnFriendRequestsSentByUserWithGivenStatus() {
            List<FriendRequest> requests = friendRequestRepository.findBySenderAndStatus(sender, EFriendRequestStatus.PENDING);

            assertThat(requests).isNotEmpty().containsExactly(friendRequest);
        }

        @Test
        void findByReceiverAndStatus_shouldReturnFriendRequestsReceivedByUserWithGivenStatus() {
            List<FriendRequest> requests = friendRequestRepository.findByReceiverAndStatus(receiver, EFriendRequestStatus.PENDING);

            assertThat(requests).isNotEmpty().containsExactly(friendRequest);
        }
    }
}
