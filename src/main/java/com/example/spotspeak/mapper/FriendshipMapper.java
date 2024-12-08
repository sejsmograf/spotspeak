package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.FriendRequest;
import com.example.spotspeak.entity.User;
import org.springframework.stereotype.Component;

@Component
public class FriendshipMapper {
    private final UserMapper userMapper;

    public FriendshipMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public FriendshipUserInfoDTO toFriendshipUserInfoDTO(FriendRequest friendship, User friend, Integer totalPoints) {
        AuthenticatedUserProfileDTO friendInfo = userMapper.createAuthenticatedUserProfileDTO(friend, totalPoints);
        return new FriendshipUserInfoDTO(
                friendship.getId(),
                friendInfo,
                friendship.getAcceptedAt());
    }
}
