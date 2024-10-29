package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.CurrentUserInfoDTO;
import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;
import org.springframework.stereotype.Component;

@Component
public class FriendshipMapper {
    private final UserMapper userMapper;

    public FriendshipMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public FriendshipUserInfoDTO toFriendshipUserInfoDTO(Friendship friendship, User friend) {
        CurrentUserInfoDTO friendInfo = userMapper.createCurrentUserInfoDTO(friend);
        return new FriendshipUserInfoDTO(
                friendship.getId(),
                friendInfo,
                friendship.getCreatedAt());
    }
}
