package com.example.spotspeak.service;

import com.example.spotspeak.dto.FriendRequestDTO;
import com.example.spotspeak.dto.FriendRequestUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.exception.*;
import com.example.spotspeak.mapper.FriendRequestMapper;
import com.example.spotspeak.repository.FriendRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendRequestService {

    FriendRequestRepository friendRequestRepository;
    FriendshipService friendshipService;
    UserProfileService userService;
    FriendRequestMapper friendRequestMapper;

    public FriendRequestService(FriendRequestRepository friendRequestRepository, FriendshipService friendshipService, UserProfileService userService, FriendRequestMapper friendRequestMapper) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.friendRequestMapper = friendRequestMapper;
    }

    @Transactional
    public FriendRequestDTO sendFriendRequest(String senderId, UUID receiverId) {
        User sender = userService.findByIdOrThrow(senderId);
        User receiver = userService.findByIdOrThrow(String.valueOf(receiverId));

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(receiver, sender, EFriendRequestStatus.PENDING)) {
            throw new FriendRequestExistsException("Request already received from this user");
        }
        if(friendRequestRepository.existsBySenderAndReceiverAndStatus(sender,receiver, EFriendRequestStatus.PENDING)) {
            throw new FriendRequestExistsException("Request already sent to this user");
        }
        if(friendshipService.checkFriendshipExists(sender, receiver)) {
            throw new FriendshipExistsException("Friendship between users already exists");
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(EFriendRequestStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        friendRequestRepository.save(friendRequest);

        return friendRequestMapper.toFriendRequestDTO(friendRequest);
    }

    @Transactional
    public FriendRequestDTO acceptFriendRequest(String currentUserId, Long requestId) {
        User currentUser = userService.findByIdOrThrow(currentUserId);
        FriendRequest friendRequest = validateAndGetPendingRequest(currentUser, requestId);

        friendRequest.setStatus(EFriendRequestStatus.ACCEPTED);
        friendRequest.setAcceptedAt(LocalDateTime.now());
        friendRequestRepository.save(friendRequest);

        friendshipService.createFriendship(friendRequest.getSender(), currentUser);

        return friendRequestMapper.toFriendRequestDTO(friendRequest);
    }

    @Transactional
    public FriendRequestDTO rejectFriendRequest(String currentUserId, Long requestId) {
        User currentUser = userService.findByIdOrThrow(currentUserId);
        FriendRequest friendRequest = validateAndGetPendingRequest(currentUser, requestId);

        friendRequest.setStatus(EFriendRequestStatus.REJECTED);
        friendRequest.setRejectedAt(LocalDateTime.now());
        friendRequestRepository.save(friendRequest);

        return friendRequestMapper.toFriendRequestDTO(friendRequest);
    }

    @Transactional
    public void cancelFriendRequest(String currentUserId, Long requestId) {
        User currentUser = userService.findByIdOrThrow(currentUserId);
        FriendRequest friendRequest = findById(requestId);

        if (!friendRequest.getSender().equals(currentUser)) {
            throw new UnauthorizedFriendRequestAccessException("User is not the sender of the friend request");
        }

        if (friendRequest.getStatus() != EFriendRequestStatus.PENDING) {
            throw new InvalidFriendRequestStatusException("Only pending friend requests can be processed.");
        }

        friendRequestRepository.delete(friendRequest);
    }

    public List<FriendRequestUserInfoDTO> getSentFriendRequests(String senderId) {
        User sender = userService.findByIdOrThrow(senderId);
        List<FriendRequest> sentRequests = friendRequestRepository.findBySenderAndStatus(sender, EFriendRequestStatus.PENDING);
        return sentRequests.stream()
                .map(request -> friendRequestMapper.toUserInfoFriendRequestDTO(request, request.getReceiver()))
                .collect(Collectors.toList());
    }

    public List<FriendRequestUserInfoDTO> getReceivedFriendRequests(String receiverId) {
        User receiver = userService.findByIdOrThrow(receiverId);
        List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverAndStatus(receiver, EFriendRequestStatus.PENDING);
        return receivedRequests.stream()
                .map(request -> friendRequestMapper.toUserInfoFriendRequestDTO(request, request.getSender()))
                .collect(Collectors.toList());
    }

    private FriendRequest findById(Long requestId) {
        return friendRequestRepository.findById(requestId).orElseThrow(
                () -> {
                    throw new FriendRequestNotFoundException("Could not find the request");
                });
    }

    private FriendRequest validateAndGetPendingRequest(User currentUser, Long requestId) {
        FriendRequest friendRequest = findById(requestId);
        userService.findByIdOrThrow(String.valueOf(friendRequest.getSender().getId()));

        if (friendRequest.getStatus() != EFriendRequestStatus.PENDING) {
            throw new InvalidFriendRequestStatusException("Only pending friend requests can be processed.");
        }

        if (!friendRequest.getReceiver().equals(currentUser)) {
            throw new UnauthorizedFriendRequestAccessException("User is not the receiver of the friend request.");
        }

        return friendRequest;
    }
}
