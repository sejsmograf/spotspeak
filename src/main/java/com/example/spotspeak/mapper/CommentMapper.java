package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CommentMapper {
    private final UserMapper userMapper;

    public CommentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CommentResponseDTO toCommentResponseDTO(Comment comment) {
        List<UUID> mentionedUserIds = comment.getMentions() != null
            ? comment.getMentions().stream()
            .map(mention -> mention.getMentionedUser().getId())
            .toList()
            : List.of();

        return new CommentResponseDTO(
                comment.getId(),
                comment.getTrace().getId(),
                userMapper.createPublicUserProfileDTO(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                mentionedUserIds
        );
    }
}
