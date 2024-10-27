package com.example.spotspeak.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.spotspeak.entity.Trace;

public interface TraceRepository extends JpaRepository<Trace, Long> {

    @Query(value = "SELECT id, ST_X(location::geometry) AS longitude, ST_Y(location::geometry) AS latitude " +
            "FROM traces " +
            "WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(?1, ?2), 4326)::geography, ?3)", nativeQuery = true)
    List<Object[]> findNearbyTracesLocations(double longitude, double latitude, double distance);

    @Query(value = "SELECT COUNT(*) > 0 " +
            "FROM traces " +
            "WHERE id = ?1 AND ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(?2, ?3), 4326)::geography, ?4)", nativeQuery = true)
    boolean isTraceWithingDistance(Long traceId, double longitude, double latitude, double distance);
}
