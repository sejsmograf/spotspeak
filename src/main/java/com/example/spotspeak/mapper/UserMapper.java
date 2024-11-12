package com.example.spotspeak.mapper;

import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.AuthenticatedUserProfileDTO;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.RegisteredUserDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.ResourceService;

@Component
public class UserMapper {
    private final ResourceService resourceService;

    public UserMapper(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public AuthenticatedUserProfileDTO createAuthenticatedUserProfileDTO(User user) {
        Resource profilePicture = user.getProfilePicture();
        String profilePictureUrl = profilePicture != null
                ? resourceService.getResourceAccessUrl(profilePicture.getId())
                : null;

        return new AuthenticatedUserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                profilePictureUrl);
    }

    public User createUserFromDTO(RegisteredUserDTO userDTO) {
        User user = User.builder()
                .id(userDTO.id())
                .firstName(userDTO.firstName())
                .lastName(userDTO.lastName())
                .username(userDTO.username())
                .email(userDTO.email())
                .registeredAt(userDTO.registeredAt())
                .build();

        return user;
    }

    public PublicUserProfileDTO createPublicUserProfileDTO(User user) {
        Resource profilePicture = user.getProfilePicture();
        String profilePictureUrl = profilePicture != null
                ? resourceService.getResourceAccessUrl(profilePicture.getId())
                : null;

        return new PublicUserProfileDTO(
                user.getId(),
                user.getUsername(),
                profilePictureUrl);
    }

    public void updateUserFromDTO(User user, UserUpdateDTO updateDTO) {
        if (updateDTO.firstName() != null) {
            user.setFirstName(updateDTO.firstName());
        }
        if (updateDTO.lastName() != null) {
            user.setLastName(updateDTO.lastName());
        }
        if (updateDTO.email() != null) {
            user.setEmail(updateDTO.email());
        }
        if (updateDTO.username() != null) {
            user.setUsername(updateDTO.username());
        }
    }
}
