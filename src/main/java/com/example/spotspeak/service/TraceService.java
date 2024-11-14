package com.example.spotspeak.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.service.achievement.UserActionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.mapper.TraceMapper;
import com.example.spotspeak.repository.TraceRepository;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

@Service
public class TraceService {

    private TraceRepository traceRepository;
    private UserService userService;
    private TraceMapper traceMapper;
    private TraceCreationService traceCreationService;
    private TraceDiscoveryService traceDiscoveryService;
    private ApplicationEventPublisher eventPublisher;

    public TraceService(TraceRepository traceRepository,
            ResourceService resourceService,
            UserService userService,
            TraceMapper traceMapper,
            TraceCreationService traceCreationService,
            TraceDiscoveryService traceDiscoveryService,
            ApplicationEventPublisher eventPublisher) {
        this.traceRepository = traceRepository;
        this.userService = userService;
        this.traceMapper = traceMapper;
        this.traceCreationService = traceCreationService;
        this.traceDiscoveryService = traceDiscoveryService;
        this.eventPublisher = eventPublisher;
    }

    public List<TraceDownloadDTO> getTracesForAuthor(String userId) {
        List<Trace> userTraces = traceRepository.findAllByAuthor(UUID.fromString(userId));
        return userTraces.stream()
                .map(traceMapper::createTraceDownloadDTO)
                .toList();
    }

    public List<TraceDownloadDTO> getDiscoveredTraces(String userId) {
        return traceDiscoveryService.findUserDiscoveredTraces(userId).stream()
                .map(traceMapper::createTraceDownloadDTO)
                .toList();
    }

    public List<TraceLocationDTO> getNearbyTracesForUser(String userId, double longitude, double latitude,
            double distance) {
        List<Object[]> results = traceRepository.findNearbyTracesLocationsForUser(UUID.fromString(userId),
                longitude, latitude, distance);

        return results.stream().map(traceMapper::createTraceLocationDtoFromNativeQueryResult).toList();
    }

    public Trace createTrace(String userId, MultipartFile file, TraceUploadDTO traceUploadDTO) {
        User author = userService.findByIdOrThrow(userId);
        Trace created = traceCreationService.createAndPersistTrace(author, file, traceUploadDTO);
        discoverTrace(userId, created.getId(), created.getLongitude(), created.getLatitude());
        return created;
    }

    @Transactional
    public void deleteTrace(Long traceId, String userId) {
        Trace trace = findByIdOrThrow(traceId);
        boolean isAuthor = trace.getAuthor().getId().toString().equals(userId);

        if (!isAuthor) {
            throw new ForbiddenException("Only author can delete trace");
        }

        trace.setResource(null);
        trace.clearDiscoverers();

        traceRepository.deleteById(traceId);
    }

    public TraceDownloadDTO discoverTrace(String userId,
            Long traceId,
            double longitude,
            double latitude) {
        User discoverer = userService.findByIdOrThrow(userId);
        Trace discovered = traceDiscoveryService.discoverTrace(discoverer, traceId, longitude, latitude);
        return traceMapper.createTraceDownloadDTO(discovered);
    }

    @Transactional
    public TraceDownloadDTO discoverTraceWithEvent(String userId,
                                          Long traceId,
                                          double longitude,
                                          double latitude) {
        User discoverer = userService.findByIdOrThrow(userId);
        Trace discovered = traceDiscoveryService.discoverTrace(discoverer, traceId, longitude, latitude);

        if(!discovered.getAuthor().equals(discoverer)) {
            UserActionEvent traceEvent = UserActionEvent.builder()
                .user(discoverer)
                .eventType(EEventType.DISCOVER_TRACE)
                .location(discovered.getLocation())
                .timestamp(LocalDateTime.now())
                .build();
            eventPublisher.publishEvent(traceEvent);
        }

        return traceMapper.createTraceDownloadDTO(discovered);
    }

    public TraceDownloadDTO getTraceInfoForUser(String userId, Long traceId) {
        Trace trace = findByIdOrThrow(traceId);
        User user = userService.findByIdOrThrow(userId);

        boolean discovered = traceDiscoveryService.hasUserDiscoveredTrace(user, trace);
        boolean isAuthor = trace.getAuthor().getId().equals(user.getId());

        boolean canGetTraceInfo = discovered || isAuthor;

        if (!canGetTraceInfo) {
            throw new ForbiddenException("User is not allowed to get trace info");
        }

        return traceMapper.createTraceDownloadDTO(trace);
    }

    public Trace findByIdOrThrow(Long traceId) {
        return traceRepository.findById(traceId).orElseThrow(
                () -> new TraceNotFoundException("Could not find trace with id: " + traceId));
    }
}
