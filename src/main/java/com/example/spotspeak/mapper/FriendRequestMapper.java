package com.example.spotspeak.mapper;

import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.FriendRequestDTO;
import com.example.spotspeak.dto.FriendRequestUserInfoDTO;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;

@Component
public class FriendRequestMapper {

    private final UserMapper userMapper;

    public FriendRequestMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public FriendRequestDTO toFriendRequestDTO(FriendRequest friendRequest) {
        return new FriendRequestDTO(
                friendRequest.getId(),
                friendRequest.getSender().getId(),
                friendRequest.getReceiver().getId(),
                friendRequest.getStatus(),
                friendRequest.getSentAt(),
                friendRequest.getAcceptedAt(),
                friendRequest.getRejectedAt());
    }

    public FriendRequestUserInfoDTO toUserInfoFriendRequestDTO(FriendRequest friendRequest, User senderOrReceiver) {
        PublicUserProfileDTO senderOrReceiverInfo = userMapper
                .createPublicUserProfileDTO(senderOrReceiver);

        return new FriendRequestUserInfoDTO(
                friendRequest.getId(),
                senderOrReceiverInfo,
                friendRequest.getStatus(),
                friendRequest.getSentAt(),
                friendRequest.getAcceptedAt(),
                friendRequest.getRejectedAt());
    }
}
