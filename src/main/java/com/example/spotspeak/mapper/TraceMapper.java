package com.example.spotspeak.mapper;

import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.enumeration.ETraceType;
import com.example.spotspeak.service.ResourceService;

@Component
public class TraceMapper {
    private final ResourceService resourceService;
    private final UserMapper userMapper;

    public TraceMapper(ResourceService resourceService, UserMapper userMapper) {
        this.resourceService = resourceService;
        this.userMapper = userMapper;
    }

    public TraceDownloadDTO createTraceDownloadDTO(Trace trace) {
        Resource resource = trace.getResource();
        String resourceUrl = resource != null
                ? resourceService.getResourceAccessUrl(resource.getId())
                : null;

        PublicUserProfileDTO author = userMapper.createPublicUserProfileDTO(trace.getAuthor());

        return new TraceDownloadDTO(
                trace.getId(),
                author,
                resourceUrl,
                trace.getDescription(),
                trace.getComments(),
                trace.getTags(),
                trace.getLatitude(),
                trace.getLongitude(),
                trace.getTraceType(),
                trace.getCreatedAt());

    }

    public TraceLocationDTO createTraceLocationDtoFromNativeQueryResult(Object[] result) {
        if (result.length != 5) {
            throw new IllegalArgumentException("Expected 5 elements in the result array, but got " + result.length);
        }
        ETraceType traceType = ETraceType.valueOf((String) result[3]);

        return new TraceLocationDTO(
                (Long) result[0],
                (Double) result[1],
                (Double) result[2],
                traceType,
                (Boolean) result[4]);
    }
}
