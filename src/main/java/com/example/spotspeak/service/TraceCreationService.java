package com.example.spotspeak.service;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Event;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.entity.enumeration.ETraceType;
import com.example.spotspeak.repository.TraceRepository;
import com.example.spotspeak.service.achievement.UserActionEvent;

import jakarta.transaction.Transactional;

@Service
public class TraceCreationService {

    private TraceRepository traceRepository;
    private ResourceService resourceService;
    private KeyGenerationService keyGenerationService;
    private GeometryFactory geometryFactory;
    private ApplicationEventPublisher eventPublisher;
    private EventService eventService;

    public TraceCreationService(TraceRepository traceRepository, ResourceService resourceService,
            KeyGenerationService keyGenerationService,
            UserService userService,
            ApplicationEventPublisher eventPublisher,
            EventService eventService) {
        this.traceRepository = traceRepository;
        this.resourceService = resourceService;
        this.geometryFactory = new GeometryFactory();
        this.eventPublisher = eventPublisher;
        this.keyGenerationService = keyGenerationService;
        this.eventService = eventService;
    }

    @Transactional
    public Trace createAndPersistTrace(User author,
            MultipartFile file,
            TraceUploadDTO dto) {
        TraceCreationComponents components = prepareTraceCreationComponents(dto);
        Resource resource = file == null ? null : processAndStoreTraceResource(author, file);
        Trace trace = buildAndSaveTrace(components, author, resource);
        UserActionEvent traceEvent = UserActionEvent.builder()
                .user(author)
                .eventType(EEventType.ADD_TRACE)
                .location(trace.getLocation())
                .timestamp(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(traceEvent);
        return trace;
    }

    private Trace buildAndSaveTrace(TraceCreationComponents components, User author, Resource resource) {
        ETraceType type = determineTraceType(resource);

        Trace trace = Trace.builder()
                .traceType(type)
                .location(components.location())
                .author(author)
                .description(components.description())
                .resource(resource)
                .associatedEvent(components.associatedEvent())
                .expiresAt(LocalDateTime.now().plusHours(TraceConstants.TRACE_EXPIRATION_HOURS))
                .build();

        return traceRepository.save(trace);
    }

    private Resource processAndStoreTraceResource(User author, MultipartFile file) {
        String authorId = author.getId().toString();
        String origialFilename = file.getOriginalFilename();
        String resourceKey = keyGenerationService.generateUniqueTraceResourceKey(authorId, origialFilename);
        return resourceService.uploadFileAndSaveResource(file, resourceKey);
    }

    private ETraceType determineTraceType(Resource resource) {
        return resource == null
                ? ETraceType.TEXTONLY
                : determineTraceResourceType(resource.getFileType());
    }

    private ETraceType determineTraceResourceType(String mimeType) {
        if (mimeType.startsWith("image")) {
            return ETraceType.PHOTO;
        } else if (mimeType.startsWith("video")) {
            return ETraceType.VIDEO;
        }

        throw new IllegalArgumentException("Invalid mime type: " + mimeType + " for trace resource");
    }

    private TraceCreationComponents prepareTraceCreationComponents(TraceUploadDTO dto) {
        Point location = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));
        location.setSRID(4326);
        String description = dto.description();
        Event associatedEvent = eventService.findEventWithinDistance(dto.longitude(), dto.latitude(),
                TraceConstants.EVENT_EPSILON_METERS);

        return new TraceCreationComponents(location, associatedEvent, description);
    }

    private record TraceCreationComponents(
            Point location,
            Event associatedEvent,
            String description) {
    }

}
