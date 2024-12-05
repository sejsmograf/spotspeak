package com.example.spotspeak.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.exception.TraceNotWithinDistanceException;
import com.example.spotspeak.repository.TraceRepository;
import com.example.spotspeak.service.achievement.UserActionEvent;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

@Service
public class TraceDiscoveryService {

    private TraceRepository traceRepository;
    private ApplicationEventPublisher eventPublisher;

    public TraceDiscoveryService(TraceRepository traceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.traceRepository = traceRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Trace> findUserDiscoveredTraces(String userId) {
        UUID convertedId = UUID.fromString(userId);
        List<Trace> discoveredTraces = traceRepository.findDiscoveredTracesByUserId(convertedId);
        return discoveredTraces;
    }

    @Transactional
    public Trace discoverTrace(User user,
            Long traceId,
            double userLongitude,
            double userLatitude) {
        boolean withinDiscoveryDistance = traceRepository.isTraceWithingDistance(traceId,
                userLongitude,
                userLatitude,
                TraceConstants.TRACE_DISCOVERY_DISTANCE);

        Trace toDiscover = traceRepository.findById(traceId).orElseGet(() -> {
            throw new TraceNotFoundException("Trace not found");
        });

        if(!toDiscover.getIsActive()) {
            throw new ForbiddenException("Trace is not active");
        }

        if (!withinDiscoveryDistance) {
            throw new TraceNotWithinDistanceException("Trace is not within discovery distance");
        }

        if (hasUserDiscoveredTrace(user, toDiscover)) {
            return toDiscover;
        }

        markTraceAsDiscovered(toDiscover, user);
        propagateTraceDiscoveryEvent(toDiscover, user);

        return traceRepository.save(toDiscover);
    }

    public void propagateTraceDiscoveryEvent(Trace discovered, User discoverer) {
        if (discoverer.equals(discovered.getAuthor())) {
            return;
        }

        UserActionEvent traceEvent = UserActionEvent.builder()
                .user(discoverer)
                .eventType(EEventType.DISCOVER_TRACE)
                .location(discovered.getLocation())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(traceEvent);
    }

    public boolean hasUserDiscoveredTrace(User user, Trace trace) {
        return user.getDiscoveredTraces().contains(trace);
    }

    private void markTraceAsDiscovered(Trace discoveredTrace, User user) {
        discoveredTrace.getDiscoverers().add(user);
        user.getDiscoveredTraces().add(discoveredTrace);
    }

}
