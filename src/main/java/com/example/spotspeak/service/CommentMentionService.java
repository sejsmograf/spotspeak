package com.example.spotspeak.service;

import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.CommentMention;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.CommentMentionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentMentionService {

    private final UserService userService;
    private final CommentMentionRepository commentMentionRepository;

    public CommentMentionService(UserService userService, CommentMentionRepository commentMentionRepository) {
        this.userService = userService;
        this.commentMentionRepository = commentMentionRepository;
    }

    public List<CommentMention> createMentions(Comment comment, List<UUID> userIds) {
        return userIds.stream()
            .map(userId -> {
                try {
                    User user = userService.findByIdOrThrow(String.valueOf(userId));
                    return CommentMention.builder()
                        .comment(comment)
                        .mentionedUser(user)
                        .build();
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    public void saveAllMentions(List<CommentMention> commentMentions) {
        commentMentionRepository.saveAll(commentMentions);
    }

    public void deleteAllMentions(List<CommentMention> commentMentions) {
        commentMentionRepository.deleteAll(commentMentions);
    }
}
