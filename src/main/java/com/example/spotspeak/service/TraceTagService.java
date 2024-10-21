package com.example.spotspeak.service;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.repository.TraceTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceTagService {

    private final TraceTagRepository traceTagRepository;

    public TraceTagService(TraceTagRepository traceTagRepository) {
        this.traceTagRepository = traceTagRepository;
    }

    public List<Tag> getTagsForTrace(Long traceId) {
        return traceTagRepository.findTagsByTraceId(traceId);
    }
}
