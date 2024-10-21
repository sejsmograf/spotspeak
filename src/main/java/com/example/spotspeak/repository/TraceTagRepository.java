package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.TraceTag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TraceTagRepository extends CrudRepository<TraceTag, Long> {
    @Query("SELECT t.tag FROM TraceTag t WHERE t.trace.id = :traceId")
    List<Tag> findTagsByTraceId(@Param("traceId") Long traceId);
}
