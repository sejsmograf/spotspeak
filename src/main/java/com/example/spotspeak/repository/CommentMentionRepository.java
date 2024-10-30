package com.example.spotspeak.repository;

import com.example.spotspeak.entity.CommentMention;
import org.springframework.data.repository.CrudRepository;

public interface CommentMentionRepository extends CrudRepository<CommentMention, Long> {
}
