package com.example.spotspeak.service;

import com.example.spotspeak.dto.FriendshipUserInfoDTO;
import com.example.spotspeak.entity.Friendship;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.FriendshipNotFoundException;
import com.example.spotspeak.mapper.FriendshipMapper;
import com.example.spotspeak.repository.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FriendshipService {
    private FriendshipRepository friendshipRepository;
    private UserService userService;
    private FriendshipMapper friendshipMapper;

    public FriendshipService(FriendshipRepository friendshipRepository, UserService userService,
            FriendshipMapper friendshipMapper) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
        this.friendshipMapper = friendshipMapper;
    }

    @Transactional
    public Friendship createFriendship(User userInitiating, User userReceiving) {
        if (checkFriendshipExists(userInitiating, userReceiving)) {
            throw new FriendshipNotFoundException("Friendship between users already exists");
        }

        Friendship friendship = Friendship.builder()
                .userInitiating(userInitiating)
                .userReceiving(userReceiving)
                .build();

        return friendshipRepository.save(friendship);
    }

    public List<FriendshipUserInfoDTO> getFriendshipDTOList(String userId) {
        User currentUser = userService.findByIdOrThrow(userId);

        List<Friendship> friendships = friendshipRepository.findAllByUser(currentUser);

        return friendships.stream()
                .map(friendship -> {
                    User friend = friendship.getUserInitiating().equals(currentUser)
                            ? friendship.getUserReceiving()
                            : friendship.getUserInitiating();

                    return friendshipMapper.toFriendshipUserInfoDTO(friendship, friend);
                })
                .toList();
    }

    public List<User> getFriends(String userId) {
        User currentUser = userService.findByIdOrThrow(userId);

        List<Friendship> friendships = friendshipRepository.findAllByUser(currentUser);

        return friendships.stream()
            .map(friendship -> friendship.getUserInitiating().equals(currentUser)
                ? friendship.getUserReceiving()
                : friendship.getUserInitiating())
            .toList();
    }


    @Transactional
    public void deleteFriend(String userId, UUID friendId) {
        User user = userService.findByIdOrThrow(userId);
        User friend = userService.findByIdOrThrow(String.valueOf(friendId));
        Friendship friendship = friendshipRepository.findByUsers(user, friend)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found between users"));
        friendshipRepository.delete(friendship);
    }

    public boolean checkFriendshipExists(User user1, User user2) {
        if (user1.equals(user2))
            return true;
        return friendshipRepository.findByUsers(user1, user2).isPresent();
    }
}
