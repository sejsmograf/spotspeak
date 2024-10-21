package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long> {

}
