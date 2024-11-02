package com.example.spotspeak.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.exception.TraceNotWithinDistanceException;
import com.example.spotspeak.repository.TraceRepository;

@Service
public class TraceDiscoveryService {

    private TraceRepository traceRepository;

    public TraceDiscoveryService(TraceRepository traceRepository) {
        this.traceRepository = traceRepository;
    }

    public List<Trace> findUserDisoveredTraces(String userId) {
        UUID convertedId = UUID.fromString(userId);
        List<Trace> discoveredTraces = traceRepository.findDiscoveredTracesByUserId(convertedId);
        return discoveredTraces;
    }

    public Trace discoverTrace(User author,
            Long traceId,
            double userLongitude,
            double userLatitude) {
        boolean withinDiscoveryDistance = traceRepository.isTraceWithingDistance(traceId,
                userLongitude,
                userLatitude,
                TraceConstants.TRACE_DISCOVERY_DISTANCE);

        if (!withinDiscoveryDistance) {
            throw new TraceNotWithinDistanceException("Trace is not within discovery distance");
        }

        Trace discoveredTrace = traceRepository.findById(traceId).orElseGet(() -> {
            throw new TraceNotFoundException("Trace not found");
        });

        markTraceAsDiscovered(discoveredTrace, author);
        return traceRepository.save(discoveredTrace);
    }

    public boolean hasUserDiscoveredTrace(User user, Trace trace) {
        return user.getDiscoveredTraces().contains(trace);
    }

    private void markTraceAsDiscovered(Trace discoveredTrace, User author) {
        discoveredTrace.getDiscoverers().add(author);
        author.getDiscoveredTraces().add(discoveredTrace);
    }

}
