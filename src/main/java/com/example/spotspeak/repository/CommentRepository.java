package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByTraceId(Long traceId);
}
