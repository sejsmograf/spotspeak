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

    @Query(value = """
            SELECT e.id
            FROM events e
            WHERE ST_DWithin(ST_SetSRID(event_center::geography, 4326),
                             ST_SetSRID(ST_MakePoint(:longitude, :latitude)::geography, 4326), :distance)
                AND e.is_active = true
    """, nativeQuery = true)
    List<Object[]> findEventsWithinDistance(double longitude, double latitude, int distance);
}
