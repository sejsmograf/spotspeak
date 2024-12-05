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
import com.example.spotspeak.mapper.EventMapper;
import com.example.spotspeak.repository.EventRepository;
import com.example.spotspeak.repository.TraceRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {

    private final TraceRepository traceRepository;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final EventNamingService eventNamingService;
    private final GeometryFactory geometryFactory;
    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    public EventService(TraceRepository traceRepository,
            EventRepository eventRepository,
            EventNamingService eventNamingService,
            EventMapper eventMapper) {
        this.traceRepository = traceRepository;
        this.eventRepository = eventRepository;
        this.eventNamingService = eventNamingService;
        this.geometryFactory = new GeometryFactory();
        this.eventMapper = eventMapper;
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
        if (!expiredEvents.isEmpty()) {
            logger.info("Deactivated " + expiredEvents.size() + " expired events.");
        }
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

    public List<EventLocationDTO> getNearbyEventsForUser(
            String userId, double longitude, double latitude, int distance) {
        List<Event> events = getNearbyEvents(longitude, latitude, distance);
        List<EventLocationDTO> eventLocations = events.stream()
                .map(event -> eventMapper.createEventLocationDTO(userId, event))
                .toList();

        return eventLocations;
    }

    public List<Event> getNearbyEvents(double longitude, double latitude, int distance) {
        List<Long> nearbyIds = eventRepository.findEventsWithinDistance(
                longitude, latitude, distance)
                .stream().map(arr -> (Long) arr[0]).toList();
        List<Event> events = (List<Event>) eventRepository.findAllById(nearbyIds);

        return events;
    }

}
