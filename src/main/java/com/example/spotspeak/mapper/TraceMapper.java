package com.example.spotspeak.mapper;

import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.service.ResourceService;

@Component
public class TraceMapper {
    private final ResourceService resourceService;

    public TraceMapper(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public TraceDownloadDTO toTraceDownloadDTO(Trace trace) {
        Resource resource = trace.getResource();
        String resourceUrl = resource != null
                ? resourceService.getResourceAccessUrl(resource.getId())
                : null;

        return new TraceDownloadDTO(
                trace.getId(),
                resourceUrl,
                trace.getDescription(),
                trace.getComments(),
                trace.getTags(),
                trace.getLatitude(),
                trace.getLongitude());
    }
}
