package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.CommentResponseDTO;
import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.enumeration.ETraceType;
import com.example.spotspeak.service.ResourceService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TraceMapper {
    private final ResourceService resourceService;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    public TraceMapper(ResourceService resourceService, UserMapper userMapper, CommentMapper commentMapper) {
        this.resourceService = resourceService;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
    }

    public TraceDownloadDTO createTraceDownloadDTO(Trace trace) {
        Resource resource = trace.getResource();
        String resourceUrl = resource != null
                ? resourceService.getResourceAccessUrl(resource.getId())
                : null;

        PublicUserProfileDTO author = userMapper.createPublicUserProfileDTO(trace.getAuthor());

        List<CommentResponseDTO> commentResponseDTOs = trace.getComments() != null
                ? trace.getComments().stream()
                        .map(commentMapper::toCommentResponseDTO)
                        .toList()
                : List.of();

        return new TraceDownloadDTO(
                trace.getId(),
                author,
                resourceUrl,
                trace.getDescription(),
                commentResponseDTOs,
                trace.getTags(),
                trace.getLatitude(),
                trace.getLongitude(),
                trace.getTraceType(),
                trace.getCreatedAt());

    }

    public TraceLocationDTO crateTraceDownloadDtoForUser(String userId, Trace trace) {
        boolean hasDiscovered = trace.getDiscoverers().stream()
                .anyMatch(user -> user.getId().toString().equals(userId));

        return new TraceLocationDTO(
                trace.getId(),
                trace.getLongitude(),
                trace.getLatitude(),
                trace.getTraceType(),
                hasDiscovered,
                trace.getCreatedAt());
    }
}
