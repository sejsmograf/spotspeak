package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTraceId(Long traceId);
}
