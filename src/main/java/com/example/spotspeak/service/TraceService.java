package com.example.spotspeak.service;

import java.util.List;
import java.util.UUID;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.*;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.mapper.TraceMapper;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import org.springframework.stereotype.Service;
import com.example.spotspeak.repository.TraceRepository;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TraceService {

    private TraceRepository traceRepository;
    private UserService userService;
    private TraceMapper traceMapper;
    private TraceCreationService traceCreationService;
    private TraceDiscoveryService traceDiscoveryService;

    public TraceService(TraceRepository traceRepository,
            ResourceService resourceService,
            UserService userService,
            TraceMapper traceMapper,
            TraceCreationService traceCreationService,
            TraceDiscoveryService traceDiscoveryService) {
        this.traceRepository = traceRepository;
        this.userService = userService;
        this.traceMapper = traceMapper;
        this.traceCreationService = traceCreationService;
        this.traceDiscoveryService = traceDiscoveryService;
    }

    public List<TraceDownloadDTO> getTracesForAuthor(String userId) {
        List<Trace> userTraces = traceRepository.findAllByAuthor(UUID.fromString(userId));
        return userTraces.stream()
                .map(traceMapper::createTraceDownloadDTO)
                .toList();
    }

    public List<TraceDownloadDTO> getDiscoveredTraces(String userId) {
        return traceDiscoveryService.findUserDisoveredTraces(userId).stream()
                .map(traceMapper::createTraceDownloadDTO)
                .toList();
    }

    public List<TraceLocationDTO> getNearbyTracesForUser(String userId, double longitude, double latitude,
            double distance) {
        List<Object[]> results = traceRepository.findNearbyTracesLocationsForUser(UUID.fromString(userId),
                longitude, latitude, distance);

        return (List<TraceLocationDTO>) results.stream()
                .map(result -> new TraceLocationDTO((Long) result[0], (Double) result[1], (Double) result[2],
                        (boolean) result[3]))
                .toList();
    }

    public Trace createTrace(String userId, MultipartFile file, TraceUploadDTO traceUploadDTO) {
        User author = userService.findByIdOrThrow(userId);
        return traceCreationService.createAndPersistTrace(author, file, traceUploadDTO);
    }

    @Transactional
    public void deleteTrace(Long traceId, String userId) {
        Trace trace = findByIdOrThrow(traceId);
        boolean isAuthor = trace.getAuthor().getId().toString().equals(userId);

        if (!isAuthor) {
            throw new ForbiddenException("Only author can delete trace");
        }

        trace.setResource(null);

        for (User user : trace.getDiscoverers()) {
            user.removeDiscoveredTrace(trace);
        }

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

    public TraceDownloadDTO getTraceInfoForUser(String userId, Long traceId) {
        Trace trace = findByIdOrThrow(traceId);
        User user = userService.findByIdOrThrow(userId);

        boolean discovered = traceDiscoveryService.hasUserDiscoveredTrace(user, trace);
        boolean isAuthor = trace.getAuthor().getId().equals(user.getId());
        System.out.println("AUTHOR" + trace.getAuthor().getId());
        System.out.println("CLAIMER" + user.getId());

        boolean canGetTraceInfo = discovered || isAuthor;

        if (!canGetTraceInfo) {
            throw new ForbiddenException("User is not allowed to get trace info");
        }

        return traceMapper.createTraceDownloadDTO(trace);
    }

    private Trace findByIdOrThrow(Long traceId) {
        return traceRepository.findById(traceId).orElseThrow(
                () -> new TraceNotFoundException("Could not find trace with id: " + traceId));
    }
}
