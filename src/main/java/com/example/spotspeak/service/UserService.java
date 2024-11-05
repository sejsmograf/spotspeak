package com.example.spotspeak.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.ChallengeResponseDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.mapper.UserMapper;
import com.example.spotspeak.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepostitory;
    private ResourceService resourceService;
    private KeycloakClientService keycloakService;
    private PasswordChallengeService passwordChallengeService;
    private UserMapper userMapper;

    public List<PublicUserProfileDTO> searchUsersByUsername(String username) {
        List<User> matchingUsers = userRepostitory.findAllByUsernameIgnoreCase(username);
        return matchingUsers.stream()
                .map(user -> userMapper.createPublicUserProfileDTO(user))
                .toList();
    }

    public AuthenticatedUserProfileDTO getUserInfo(String userId) {
        User user = findByIdOrThrow(userId);
        return userMapper.createAuthenticatedUserProfileDTO(user);
    }

    public ChallengeResponseDTO generatePasswordChallenge(String userId, String password) {
        User user = findByIdOrThrow(userId);
        keycloakService.validatePasswordOrThrow(userId, password);
        String token = passwordChallengeService.createAndStoreChallenge(user.getId());
        return new ChallengeResponseDTO(Instant.now(), user.getId(), token);
    }

    public void updateUserPassword(String userId, PasswordUpdateDTO dto) {
        keycloakService.updatePassword(userId, dto);
    }

    @Transactional
    public void deleteById(String userIdString) {
        User user = findByIdOrThrow(userIdString);

        userRepostitory.deleteById(user.getId());
        userRepostitory.flush();

        keycloakService.deleteUser(userIdString);
    }

    @Transactional
    public User updateUser(String userIdString, UserUpdateDTO updateDTO) {
        User user = findByIdOrThrow(userIdString);
        String challengeToken = updateDTO.passwordChallengeToken();
        passwordChallengeService.verifyChallengeOrThrow(challengeToken, user.getId());

        userMapper.updateUserFromDTO(user, updateDTO);

        keycloakService.updateUser(userIdString, updateDTO);
        return user;
    }

    public Resource updateUserProfilePicture(String userIdString, MultipartFile file) {
        User user = findByIdOrThrow(userIdString);
        deleteUserProfilePicture(userIdString);

        Resource resource = resourceService.uploadUserProfilePicture(userIdString, file);
        user.setProfilePicture(resource);
        userRepostitory.save(user);
        return resource;
    }

    public void deleteUserProfilePicture(String userId) {
        User user = findByIdOrThrow(userId);
        Resource profilePicture = user.getProfilePicture();

        if (profilePicture != null) {
            user.setProfilePicture(null);
            resourceService.deleteResource(profilePicture.getId());
        }
    }

    private User findByIdOrThrow(UUID userId) {
        return userRepostitory.findById(userId).orElseThrow(
                () -> {
                    throw new UserNotFoundException("Could not find the user");
                });
    }

    private UUID userIdToUUID(String userId) {
        try {
            UUID convertedId = UUID.fromString(userId);
            return convertedId;
        } catch (IllegalArgumentException e) {
            throw new UserNotFoundException("Invalid userId format");
        }
    }

    public User findByIdOrThrow(String userIdString) {
        UUID convertedId = userIdToUUID(userIdString);
        return findByIdOrThrow(convertedId);
    }
}
