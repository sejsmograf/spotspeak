package com.example.spotspeak.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.service.achievement.UserActionEvent;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.TraceRepository;

import jakarta.transaction.Transactional;

@Service
public class TraceCreationService {

    private TraceRepository traceRepository;
    private ResourceService resourceService;
    private TagService tagService;
    private GeometryFactory geometryFactory;
    private ApplicationEventPublisher eventPublisher;

    public TraceCreationService(TraceRepository traceRepository, ResourceService resourceService,
            UserService userService, TagService tagService, ApplicationEventPublisher eventPublisher) {
        this.traceRepository = traceRepository;
        this.resourceService = resourceService;
        this.tagService = tagService;
        this.geometryFactory = new GeometryFactory();
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Trace createAndPersistTrace(User author,
            MultipartFile file,
            TraceUploadDTO dto) {
        TraceCreationComponents components = prepareTraceCreationComponents(dto);
        Resource resource = processTraceResource(author, file);
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
        Trace trace = Trace.builder()
                .location(components.location())
                .author(author)
                .description(components.description())
                .tags(components.tags())
                .resource(resource)
                .build();

        return traceRepository.save(trace);
    }

    private Resource processTraceResource(User author, MultipartFile file) {
        Resource resource = file == null
                ? null
                : resourceService.uploadTraceResource(author.getId().toString(), file);

        return resource;
    }

    private TraceCreationComponents prepareTraceCreationComponents(TraceUploadDTO dto) {
        Point location = geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude()));
        List<Tag> tags = dto.tagIds() == null
                ? null
                : tagService.findAllByIds(dto.tagIds());

        String description = dto.description();
        return new TraceCreationComponents(tags, location, description);
    }

    private record TraceCreationComponents(
            List<Tag> tags,
            Point location,
            String description) {
    }

}
