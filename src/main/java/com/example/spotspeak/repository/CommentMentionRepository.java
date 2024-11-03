package com.example.spotspeak.repository;

import com.example.spotspeak.entity.CommentMention;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentMentionRepository extends CrudRepository<CommentMention, Long> {
    List<CommentMention> findByCommentId(Long commentId);
}
