package com.example.spotspeak.service;

import com.example.spotspeak.constants.CommentMentionConstants;
import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.CommentMention;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.CommentMentionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class CommentMentionService {

    private final UserService userService;
    private final CommentMentionRepository commentMentionRepository;

    public CommentMentionService(UserService userService, CommentMentionRepository commentMentionRepository) {
        this.userService = userService;
        this.commentMentionRepository = commentMentionRepository;
    }

    public List<CommentMention> processMentions(Comment comment) {
        List<String> usernames = extractMentions(comment.getContent());
        List<User> mentionedUsers = userService.findUsersByUsernames(usernames);

        return mentionedUsers.stream()
                .map(user -> CommentMention.builder()
                        .comment(comment)
                        .mentionedUser(user)
                        .build())
                .collect(Collectors.toList());
    }

    public void saveAllMentions(List<CommentMention> commentMentions) {
        commentMentionRepository.saveAll(commentMentions);
    }

    public void deleteAllMentions(List<CommentMention> commentMentions) {
        commentMentionRepository.deleteAll(commentMentions);
    }

    private List<String> extractMentions(String content) {
        Matcher matcher = CommentMentionConstants.MENTION_PATTERN.matcher(content);
        return matcher.results()
                .map(match -> match.group(1))
                .toList();
    }
}
