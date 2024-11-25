package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Long> {

    @Query(value = """
            SELECT e
            FROM Event e
            WHERE e.expiresAt < :now
            AND e.isActive = true
            """)
    List<Event> findExpiredEvents(LocalDateTime now);
}
