package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
    private final UserMapper userMapper;

    public CommentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CommentResponseDTO toCommentResponseDTO(Comment comment) {
        List<UUID> mentionIds = comment.getMentions().stream()
                .map(mention -> mention.getMentionedUser().getId())
                .collect(Collectors.toList());

        return new CommentResponseDTO(
                comment.getId(),
                comment.getTrace().getId(),
                userMapper.createPublicUserProfileDTO(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                mentionIds
        );
    }
}
