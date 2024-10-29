package com.example.spotspeak.mapper;

import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.CurrentUserInfoDTO;
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

    public CurrentUserInfoDTO createCurrentUserInfoDTO(User user) {
        Resource profilePicture = user.getProfilePicture();
        String profilePictureUrl = profilePicture != null
                ? resourceService.getResourceAccessUrl(profilePicture.getId())
                : null;

        return new CurrentUserInfoDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
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
