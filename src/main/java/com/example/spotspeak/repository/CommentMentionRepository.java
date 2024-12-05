package com.example.spotspeak.repository;

import com.example.spotspeak.entity.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
    List<CommentMention> findByCommentId(Long commentId);
}
