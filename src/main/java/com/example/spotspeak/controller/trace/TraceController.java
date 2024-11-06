package com.example.spotspeak.controller.trace;

import com.example.spotspeak.constants.FileUploadConsants;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.mapper.TraceMapper;
import com.example.spotspeak.service.TagService;
import com.example.spotspeak.service.TraceService;
import com.example.spotspeak.validation.ValidFile;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/traces")
@Validated
public class TraceController {

    private TraceService traceService;
    private TagService tagService;
    private TraceMapper mapper;

    public TraceController(TraceService traceService,
            TagService tagService,
            TraceMapper mapper) {
        this.traceService = traceService;
        this.tagService = tagService;
        this.mapper = mapper;
    }

    @GetMapping("/my")
    public ResponseEntity<List<TraceDownloadDTO>> getMyTraces(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<TraceDownloadDTO> usersTraces = traceService.getTracesForAuthor(userId);
        return ResponseEntity.ok(usersTraces);
    }

    @GetMapping("/discovered")
    public ResponseEntity<List<TraceDownloadDTO>> getDiscoveredTraces(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<TraceDownloadDTO> usersTraces = traceService.getDiscoveredTraces(userId);
        return ResponseEntity.ok(usersTraces);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<TraceLocationDTO>> getTracesNearby(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam int distance) {
        String userId = jwt.getSubject();
        List<TraceLocationDTO> nearbyTraces = traceService.getNearbyTracesForUser(userId, longitude, latitude,
                distance);
        return ResponseEntity.ok(nearbyTraces);
    }

    @GetMapping("/{traceId}")
    public ResponseEntity<TraceDownloadDTO> getTraceInfo(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long traceId) {
        String userId = jwt.getSubject();
        TraceDownloadDTO traceInfo = traceService.getTraceInfoForUser(userId, traceId);
        return ResponseEntity.ok(traceInfo);
    }

    @GetMapping("/discover/{traceId}")
    public ResponseEntity<TraceDownloadDTO> discoverTrace(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long traceId,
            @RequestParam double currentLongitude, @RequestParam double currentLatitude) {
        String userId = jwt.getSubject();
        TraceDownloadDTO traceInfo = traceService.discoverTrace(userId, traceId, currentLongitude,
                currentLatitude);
        return ResponseEntity.ok(traceInfo);
    }

    @PostMapping
    public ResponseEntity<TraceDownloadDTO> createTrace(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ValidFile(required = false, maxSize = FileUploadConsants.TRACE_MEDIA_MAX_SIZE, allowedTypes = {
                    "image/jpeg", "image/png", "image/jpg", "image/gif", "image/heic", "image/heif", "video/mp4",
                    "video/3pg", "video/quicktime" }) @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart @Valid TraceUploadDTO traceUploadDTO) {
        String userId = jwt.getSubject();
        Trace trace = traceService.createTrace(userId, file, traceUploadDTO);
        TraceDownloadDTO dto = mapper.createTraceDownloadDTO(trace);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{traceId}")
    public ResponseEntity<Void> deleteTrace(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long traceId) {
        String userId = jwt.getSubject();
        traceService.deleteTrace(traceId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getTags() {
        List<Tag> tags = tagService.findAll();
        return ResponseEntity.ok(tags);
    }
}
