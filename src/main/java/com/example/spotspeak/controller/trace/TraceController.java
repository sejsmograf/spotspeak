package com.example.spotspeak.controller.trace;

import com.example.spotspeak.constants.FileUploadConsants;
import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceResponse;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.mapper.TraceMapper;
import com.example.spotspeak.service.TraceService;
import com.example.spotspeak.validation.ValidFile;
import io.swagger.v3.oas.annotations.media.Schema;
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
        private TraceMapper mapper;

        public TraceController(TraceService traceService,
                        TraceMapper mapper) {
                this.traceService = traceService;
                this.mapper = mapper;
        }

        @GetMapping("/my")
        public ResponseEntity<List<Trace>> getMyTraces(@AuthenticationPrincipal Jwt jwt) {
                String userId = jwt.getSubject();
                List<Trace> usersTraces = traceService.getTracesForAuthor(userId);
                return ResponseEntity.ok(usersTraces);
        }

        @GetMapping("/nearby")
        public ResponseEntity<List<TraceLocationDTO>> getTracesNearby(
                        @RequestParam double longitude, @RequestParam double latitude, @RequestParam int distance) {
                List<TraceLocationDTO> nearbyTraces = traceService.getNearbyTraces(longitude, latitude, distance);
                return ResponseEntity.ok(nearbyTraces);
        }

        @GetMapping("/{traceId}")
        @Schema(description = "For debug features, users should 'discover' traces in order to download them "
                        + "(look for /api/traces/discover/{traceId} endpoint)")
        public ResponseEntity<TraceDownloadDTO> getTraceInfo(@AuthenticationPrincipal Jwt jwt,
                        @PathVariable Long traceId) {

                String userId = jwt.getSubject();
                TraceDownloadDTO traceInfo = traceService.getTraceInfo(userId, traceId);
                return ResponseEntity.ok(traceInfo);
        }

        @GetMapping("/discover/{traceId}")
        @Schema(description = "This endpoint checks if the user is within the trace's radius and returns the trace's information. "
                        + "currently the default discovery radius is "
                        + TraceConstants.TRACE_DISCOVERY_DISTANCE
                        + " meters")
        public ResponseEntity<TraceDownloadDTO> discoverTrace(@PathVariable Long traceId,
                        @RequestParam double currentLongitude, @RequestParam double currentLatitude) {

                TraceDownloadDTO traceInfo = traceService.discoverTrace(traceId, currentLongitude, currentLatitude);
                return ResponseEntity.ok(traceInfo);
        }

        @PostMapping
        public ResponseEntity<TraceDownloadDTO> createTrace(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @ValidFile(required = false, maxSize = FileUploadConsants.TRACE_MEDIA_MAX_SIZE, allowedTypes = {
                                        "image/jpeg", "image/png",
                                        "image/jpg" }) @RequestPart(value = "file", required = false) MultipartFile file,
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
                List<Tag> tags = traceService.getAllTags();
                return ResponseEntity.ok(tags);
        }
}
