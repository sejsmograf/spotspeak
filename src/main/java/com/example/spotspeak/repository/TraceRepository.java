package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Trace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TraceRepository extends JpaRepository<Trace, Long> {

    // returns an array [Long id, Double longitude, Double latitude]
    @Query(value = "SELECT id, ST_X(location::geometry) AS longitude, ST_Y(location::geometry) AS latitude"
            + " FROM traces WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(?1,"
            + " ?2), 4326)::geography, ?3)", nativeQuery = true)
    List<Object[]> findNearbyTracesLocations(double longitude, double latitude, double distance);

    @Query(value = "SELECT t.id, ST_X(t.location::geometry) AS longitude, ST_Y(t.location::geometry) AS latitude, "
            + "t.trace_type AS trace_type, dt.user_id IS NOT NULL AS has_discovered "
            + "FROM traces t left join discovered_traces dt "
            + "ON t.id = dt.trace_id AND dt.user_id = :userId "
            + "WHERE ST_DWithin(ST_SetSRID(location::geography, 4326), "
            + "ST_SetSRID(ST_MakePoint(:longitude, :latitude)::geography, 4326), :distance)", nativeQuery = true)
    List<Object[]> findNearbyTracesLocationsForUser(
            UUID userId, double longitude, double latitude, double distance);

    @Query("SELECT t " + "FROM Trace t join t.discoverers d " + "WHERE d.id = :userId")
    List<Trace> findDiscoveredTracesByUserId(UUID userId);

    @Modifying
    @Query("UPDATE Trace t SET t.isActive = false WHERE t.isActive = true AND t.expiresAt < :currentTime")
    int deactivateExpiredTraces(LocalDateTime currentTime);

    @Query(value = "SELECT COUNT(*) > 0 FROM traces WHERE id = ?1 AND ST_DWithin(location::geography,"
            + " ST_SetSRID(ST_MakePoint(?2, ?3), 4326)::geography, ?4)", nativeQuery = true)
    boolean isTraceWithingDistance(Long traceId, double longitude, double latitude, double distance);

    @Query(value = "SELECT t " + "FROM Trace t " + "WHERE t.author.id = ?1")
    List<Trace> findAllByAuthor(UUID userId);
}
