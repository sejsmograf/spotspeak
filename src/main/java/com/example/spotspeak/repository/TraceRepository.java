package com.example.spotspeak.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.spotspeak.entity.Trace;

public interface TraceRepository extends JpaRepository<Trace, Long> {

    @Query(value = "SELECT * " +
            "FROM traces " +
            "WHERE ST_DWithin(" +
            "location::geography, " +
            "ST_SetSRID(ST_MakePoint(?1, ?2), 4326)::geography, " +
            "?3)", nativeQuery = true)
    List<Trace> getNearbyTraces(double longitude, double latitude, int distance);

    @Query(value = "SELECT id, ST_X(location::geometry) AS longitude, ST_Y(location::geometry) AS latitude " +
            "FROM traces " +
            "WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(?1, ?2), 4326)::geography, ?3)", nativeQuery = true)
    List<Object[]> findNearbyLocations(double longitude, double latitude, double distance);
}
