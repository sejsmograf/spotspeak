package com.example.spotspeak.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.example.spotspeak.entity.Trace;

public interface TraceRepository extends CrudRepository<Trace, Long> {

    @Query(value = "SELECT * " +
            "FROM traces " +
            "WHERE ST_DWithin(" +
            "location::geography, " +
            "ST_SetSRID(ST_MakePoint(?1, ?2), 4326)::geography, " +
            "?3)", nativeQuery = true)
    List<Trace> getNearbyTraces(double longitude, double latitude, int distance);
}
