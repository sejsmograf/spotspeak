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
import com.example.spotspeak.dto.EventLocationDTO;
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
    private final EventNamingService eventNamingService;
    private final GeometryFactory geometryFactory;
    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    public EventService(TraceRepository traceRepository, EventRepository eventRepository,
            EventNamingService eventNamingService) {
        this.traceRepository = traceRepository;
        this.eventRepository = eventRepository;
        this.eventNamingService = eventNamingService;
        this.geometryFactory = new GeometryFactory();
    }

    @Scheduled(fixedRate = TraceConstants.EVENT_DETECTION_INTERVAL_MS, initialDelay = 1000 * 20)
    @Transactional
    public void detectAndCreateEvents() {
        List<TraceClusterMapping> traceClusters = traceRepository.findTraceClusters(
                TraceConstants.EVENT_EPSILON_METERS, TraceConstants.EVENT_MIN_POINTS);

        for (TraceClusterMapping cluster : traceClusters) {
            logger.info("Detected " + traceClusters.size() + " trace events.");
            createAndPersistTraceEvent(cluster);
        }
    }

    @Scheduled(fixedRate = TraceConstants.EXPIRED_EVENT_CLEANUP_INTERVAL_MS, initialDelay = 1000 * 20)
    @Transactional
    public void deactivateExpiredEvents() {
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
        List<Trace> traces = traceRepository.findAllById(cluster.traceIds());
        Point center = geometryFactory.createPoint(
                new Coordinate(cluster.centroidLon(), cluster.centroidLat()));
        center.setSRID(4326);
        String eventName = eventNamingService.getEventName(traces);

        Event event = eventRepository.save(
                Event.builder()
                        .name(eventName)
                        .eventCenter(center)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusHours(TraceConstants.EVENT_EXPIRATION_HOURS))
                        .build());

        traceRepository.associateTracesWithEvent(event, cluster.traceIds());

        logger.info("Created event with id " + event.getId() + " and name " + event.getName());
        return event;
    }

    public List<EventLocationDTO> getNearbyEvents(double longitude, double latitude, int distance) {
        List<Long> nearbyIds = eventRepository.findEventsWithinDistance(
                longitude, latitude, distance)
                .stream().map(arr -> (Long) arr[0]).toList();

        List<Event> events = (List<Event>) eventRepository.findAllById(nearbyIds);

        return events.stream().map(
                e -> new EventLocationDTO(
                        e.getId(),
                        e.getEventCenter().getX(),
                        e.getEventCenter().getY(),
                        e.getName(),
                        e.getIsActive()))
                .toList();
    }

}
