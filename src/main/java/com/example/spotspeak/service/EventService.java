package com.example.spotspeak.service;

import java.time.LocalDateTime;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.dto.TraceClusterMapping;
import com.example.spotspeak.entity.Event;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.repository.EventRepository;
import com.example.spotspeak.repository.TraceRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {

    private final TraceRepository traceRepository;
    private final EventRepository eventRepository;
    private final GeometryFactory geometryFactory;
    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    public EventService(TraceRepository traceRepository, EventRepository eventRepository) {
        this.traceRepository = traceRepository;
        this.eventRepository = eventRepository;
        this.geometryFactory = new GeometryFactory();
    }

    @Scheduled(fixedRate = 1000 * 5)
    @Transactional
    public void detectAndCreateEvents() {
        logger.info("Detecting trace events...");
        List<TraceClusterMapping> traceClusters = traceRepository.findTraceClusters(
                TraceConstants.EVENT_EPSILON_METERS, TraceConstants.EVENT_MIN_POINTS);
        logger.info("Detected " + traceClusters.size() + " trace events.");

        for (TraceClusterMapping cluster : traceClusters) {
            createAndPersistTraceEvent(cluster);
        }
    }

    @Scheduled(fixedRate = 1000 * 20)
    @Transactional
    public void deactivateExpiredEvents() {
        logger.info("Deactivating expired events...");
        List<Event> expiredEvents = eventRepository.findExpiredEvents(LocalDateTime.now());
        logger.info("Deactivated " + expiredEvents.size() + " expired events.");
        expiredEvents.forEach(this::deactivateEvent);
    }

    public Event findEventWithinDistance(double longitude, double latitude, int distance) {
        Long closestTraceId = traceRepository.findClosestTraceId(longitude, latitude, distance);
        if (closestTraceId == null) {
            return null;
        }

        Trace closestTrace = traceRepository.findById(closestTraceId).get();
        return closestTrace.getAssociatedEvent();
    }

    private void deactivateEvent(Event event) {
        event.setIsActive(false);
        event.getAssociatedTraces().forEach(trace -> trace.setAssociatedEvent(null));
    }

    private Event createAndPersistTraceEvent(TraceClusterMapping cluster) {
        Point center = geometryFactory.createPoint(
                new Coordinate(cluster.centroidLon(), cluster.centroidLat()));
        center.setSRID(4326);

        Event event = eventRepository.save(
                Event.builder()
                        .name("new event")
                        .eventCenter(center)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusHours(TraceConstants.EVENT_EXPIRATION_HOURS))
                        .build());

        traceRepository.associateTracesWithEvent(event, cluster.traceIds());

        logger.info("Created event with id " + event.getId());
        return event;
    }

}
