package com.example.spotspeak.repository;

import com.example.spotspeak.dto.TraceClusterMapping;
import com.example.spotspeak.entity.Event;
import com.example.spotspeak.entity.Trace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TraceRepository extends JpaRepository<Trace, Long> {

    // returns an array [Long id, Double longitude, Double latitude]
    @Query(value = """
            SELECT id, ST_X(location::geometry) AS longitude, ST_Y(location::geometry) AS latitude
            FROM traces
            WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(?1, ?2), 4326)::geography, ?3)
            """, nativeQuery = true)
    List<Object[]> findNearbyTracesLocations(double longitude, double latitude, double distance);

    @Query(value = """
            SELECT t.id, ST_X(t.location::geometry) AS longitude, ST_Y(t.location::geometry) AS latitude,
                   t.trace_type AS trace_type, dt.user_id IS NOT NULL AS has_discovered
            FROM traces t
            LEFT JOIN discovered_traces dt
            ON t.id = dt.trace_id AND dt.user_id = :userId
            WHERE ST_DWithin(ST_SetSRID(location::geography, 4326),
                             ST_SetSRID(ST_MakePoint(:longitude, :latitude)::geography, 4326), :distance)
            """, nativeQuery = true)
    List<Object[]> findNearbyTracesLocationsForUser(
            UUID userId, double longitude, double latitude, double distance);

    @Query("""
            SELECT t
            FROM Trace t
            JOIN t.discoverers d
            WHERE d.id = :userId
            """)
    List<Trace> findDiscoveredTracesByUserId(UUID userId);

    @Modifying
    @Query("""
            UPDATE Trace t
            SET t.isActive = false
            WHERE t.isActive = true AND t.expiresAt < :currentTime
            """)
    int deactivateExpiredTraces(LocalDateTime currentTime);

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM traces
            WHERE id = ?1 AND ST_DWithin(location::geography,
                                         ST_SetSRID(ST_MakePoint(?2, ?3), 4326)::geography, ?4)
            """, nativeQuery = true)
    boolean isTraceWithingDistance(Long traceId, double longitude, double latitude, double distance);

    @Query("""
            SELECT t
            FROM Trace t
            WHERE t.author.id = ?1
            """)
    List<Trace> findAllByAuthor(UUID userId);

    @Query(value = """
            SELECT cid AS clusterId,
                ST_X(ST_Centroid(ST_Collect(location))) AS centroidLon,
                ST_Y(ST_Centroid(ST_Collect(location))) AS centroidLat,
                array_agg(id) as traceIds
            FROM
            (
                SELECT id, location,  ST_ClusterDBScan(ST_Transform(location, 3857), :epsilonMeters, :minPoints)
                    OVER () AS cid
                FROM traces
                WHERE is_active = true AND event_id is NULL
            )
            GROUP BY cid
            HAVING cid IS NOT NULL
                """, nativeQuery = true)
    List<Object[]> findTraceClustersRaw(int epsilonMeters, int minPoints);

    default List<TraceClusterMapping> findTraceClusters(int epsilonMeters, int minPoints) {
        List<TraceClusterMapping> result = new ArrayList<>();
        List<Object[]> rawClusters = findTraceClustersRaw(epsilonMeters, minPoints);

        for (Object[] rawCluster : rawClusters) {
            Long clusterId = ((Number) rawCluster[0]).longValue();
            Double centroidLon = (Double) rawCluster[1];
            Double centroidLat = (Double) rawCluster[2];
            List<Long> traceIds = Arrays.asList((Long[]) rawCluster[3]);

            result.add(new TraceClusterMapping(clusterId, traceIds, centroidLon, centroidLat));
        }

        return result;
    }

    @Modifying
    @Query("""
            UPDATE Trace t
            SET t.associatedEvent = :event
            WHERE t.id IN :traceIds
            """)
    void associateTracesWithEvent(Event event, List<Long> traceIds);

    @Query(value = """
            SELECT id FROM
            (
                SELECT id,
                       ST_Distance(
                           location::geography,
                           ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                       ) as distance
                FROM traces
                WHERE ST_DWithin(
                          location::geography,
                          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :maxDistanceMeters
                      )
            )
            ORDER BY distance asc
            LIMIT 1
            """, nativeQuery = true)
    Long findClosestTraceId(Double longitude, Double latitude, int maxDistanceMeters);
}
