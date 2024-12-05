package com.example.spotspeak.mapper;

import com.example.spotspeak.dto.CommentMentionDTO;
import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.CommentMention;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {
    private final UserMapper userMapper;

    public CommentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CommentResponseDTO toCommentResponseDTO(Comment comment) {
        List<CommentMentionDTO> mentionedUsers = comment.getMentions() != null
            ? comment.getMentions().stream()
            .map(this::toCommentMentionDTO)
            .toList()
            : List.of();

        return new CommentResponseDTO(
                comment.getId(),
                comment.getTrace().getId(),
                userMapper.createPublicUserProfileDTO(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                mentionedUsers
        );
    }

    private CommentMentionDTO toCommentMentionDTO(CommentMention mention) {
        return new CommentMentionDTO(
            mention.getMentionedUser().getId(),
            mention.getMentionedUser().getUsername()
        );
    }
}
