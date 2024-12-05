package com.example.spotspeak.dto;

import java.util.List;

public record TraceClusterMapping(
        Long clusterId,
        List<Long> traceIds,
        Double centroidLon,
        Double centroidLat) {
}
