package com.example.spotspeak.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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
    private KeyGenerationService keyGenerationService;
    private GeometryFactory geometryFactory;

    public TraceCreationService(
            TraceRepository traceRepository,
            ResourceService resourceService,
            TagService tagService,
            KeyGenerationService keyGenerationService) {
        this.traceRepository = traceRepository;
        this.resourceService = resourceService;
        this.tagService = tagService;
        this.keyGenerationService = keyGenerationService;

        geometryFactory = new GeometryFactory();
    }

    @Transactional
    public Trace createAndPersistTrace(User author,
            MultipartFile file,
            TraceUploadDTO dto) {
        TraceCreationComponents components = prepareTraceCreationComponents(dto);
        Resource resource = processAndStoreTraceResource(author, file);
        return buildAndSaveTrace(components, author, resource);
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

    private Resource processAndStoreTraceResource(User author, MultipartFile file) {
        if (file != null) {
            String authorId = author.getId().toString();
            String origialFilename = file.getOriginalFilename();
            String resourceKey = keyGenerationService.generateUniqueTraceResourceKey(authorId, origialFilename);
            return resourceService.uploadFileAndSaveResource(file, resourceKey);
        }

        return null;
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
