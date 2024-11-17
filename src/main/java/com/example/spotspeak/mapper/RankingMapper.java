package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.RankingDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.ResourceService;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {
    private final ResourceService resourceService;

    public RankingMapper(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public RankingDTO createRankingDTO(User user) {
        Resource profilePicture = user.getProfilePicture();
        String profilePictureUrl = profilePicture != null
            ? resourceService.getResourceAccessUrl(profilePicture.getId())
            : null;

        return new RankingDTO(
            0,
            user.getId(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            profilePictureUrl,
            0);
    }
}
