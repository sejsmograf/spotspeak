package com.example.spotspeak.controller.comment;

import com.example.spotspeak.dto.CommentRequestDTO;
import com.example.spotspeak.dto.CommentResponseDTO;
import com.example.spotspeak.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traces/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{traceId}")
    public ResponseEntity<CommentResponseDTO> addComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long traceId,
            @RequestBody CommentRequestDTO commentRequest) {

        String userId = jwt.getSubject();
        CommentResponseDTO commentResponseDTO = commentService.addComment(userId, traceId, commentRequest);

        return ResponseEntity.ok(commentResponseDTO);
    }

    @GetMapping("/{traceId}")
    public ResponseEntity<List<CommentResponseDTO>> getTraceComments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long traceId) {
        String userId = jwt.getSubject();
        List<CommentResponseDTO> comments = commentService.getTraceComments(userId, traceId);

        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO commentRequest
    ) {
        String userId = jwt.getSubject();
        CommentResponseDTO commentResponseDTO = commentService.updateComment(userId, commentId, commentRequest);

        return ResponseEntity.ok(commentResponseDTO);
    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long commentId
    ) {
        String userId = jwt.getSubject();
        commentService.deleteComment(userId, commentId);

        return ResponseEntity.noContent().build();
    }


}
