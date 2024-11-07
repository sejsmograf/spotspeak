package com.example.spotspeak.service;

import com.example.spotspeak.dto.CommentRequestDTO;
import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.entity.Comment;
import com.example.spotspeak.entity.CommentMention;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.CommentNotFoundException;
import com.example.spotspeak.mapper.CommentMapper;
import com.example.spotspeak.repository.CommentRepository;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private CommentRepository commentRepository;
    private UserService userService;
    private TraceService traceService;
    private CommentMentionService mentionService;
    private CommentMapper commentMapper;
    private CommentMentionService commentMentionService;

    public CommentService(CommentRepository commentRepository, UserService userService, TraceService traceService, CommentMentionService mentionService, CommentMapper commentMapper, CommentMentionService commentMentionService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.traceService = traceService;
        this.mentionService = mentionService;
        this.commentMapper = commentMapper;
        this.commentMentionService = commentMentionService;
    }

    @Transactional
    public CommentResponseDTO addComment(String userId, Long traceId, CommentRequestDTO commentRequest) {
        User author = userService.findByIdOrThrow(userId);
        Trace trace = traceService.findByIdOrThrow(traceId);

        Comment comment = Comment.builder()
                .author(author)
                .trace(trace)
                .content(commentRequest.content())
                .createdAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        List<CommentMention> commentMentions = mentionService.processMentions(comment);
        commentMentionService.saveAllMentions(commentMentions);

        comment.setMentions(commentMentions);
        comment = commentRepository.save(comment);

        return commentMapper.toCommentResponseDTO(comment);
    }

    public List<CommentResponseDTO> getTraceComments(String userId, Long traceId) {
        userService.findByIdOrThrow(userId);
        traceService.findByIdOrThrow(traceId);

        List<Comment> comments = commentRepository.findByTraceId(traceId);

        return comments.stream()
                .map(comment -> commentMapper.toCommentResponseDTO(comment))
                .toList();
    }

    @Transactional
    public CommentResponseDTO updateComment(String userId, Long commentId, CommentRequestDTO commentRequest) {
        userService.findByIdOrThrow(userId);
        Comment comment = findByIdOrThrow(commentId);
        if (!canUserModifyComment(comment, userId)) {
            throw new ForbiddenException("Only author can update comment");
        }

        comment.setContent(commentRequest.content());
        comment = commentRepository.save(comment);

        commentMentionService.deleteAllMentions(comment.getMentions());
        List<CommentMention> commentMentions = mentionService.processMentions(comment);
        commentMentionService.saveAllMentions(commentMentions);

        comment.setMentions(commentMentions);
        comment = commentRepository.save(comment);

        return commentMapper.toCommentResponseDTO(comment);
    }

    @Transactional
    public void deleteComment(String userId, Long commentId) {
        userService.findByIdOrThrow(userId);
        Comment comment = findByIdOrThrow(commentId);
        if (!canUserModifyComment(comment, userId)) {
            throw new ForbiddenException("Only author can delete comment");
        }

        commentRepository.deleteById(commentId);
    }

    private Comment findByIdOrThrow(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new CommentNotFoundException("Could not find comment with id: " + commentId));
    }

    private boolean canUserModifyComment(Comment comment, String userId) {
        return comment.getAuthor().getId().equals(UUID.fromString(userId));
    }
}
