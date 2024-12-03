package com.example.spotspeak.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.spotspeak.dto.EventLocationDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.entity.Event;

@Component
public class EventMapper {

    private final TraceMapper traceMapper;

    public EventMapper(TraceMapper traceMapper) {
        this.traceMapper = traceMapper;
    }

    public EventLocationDTO createEventLocationDTO(String userId, Event event) {
        List<TraceLocationDTO> traces = event.getAssociatedTraces().stream()
                .map(t -> traceMapper.crateTraceDownloadDtoForUser(userId, t))
                .toList();

        return new EventLocationDTO(
                event.getId(),
                event.getEventCenter().getX(),
                event.getEventCenter().getY(),
                event.getName(),
                event.getIsActive(),
                traces);
    }
}
