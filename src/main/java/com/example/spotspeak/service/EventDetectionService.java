package com.example.spotspeak.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.dto.TraceClusterMapping;
import com.example.spotspeak.repository.EventRepository;
import com.example.spotspeak.repository.TraceRepository;

import jakarta.transaction.Transactional;

@Service
public class EventDetectionService {

    TraceRepository traceRepository;
    EventRepository eventRepository;

    public EventDetectionService(TraceRepository traceRepository, EventRepository eventRepository) {
        this.traceRepository = traceRepository;
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 1000 * 5)
    @Transactional
    public void findTraceClusters() {
        List<TraceClusterMapping> traceClusters = traceRepository.findTraceClusters(
                TraceConstants.EVENT_EPSILON_METERS, TraceConstants.EVENT_MIN_POINTS);

        Set<Long> clusters = new HashSet<>(traceClusters.stream().map(c -> c.clusterId()).toList());
        System.out.println(clusters);
    }
}
