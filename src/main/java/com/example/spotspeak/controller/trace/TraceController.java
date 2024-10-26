package com.example.spotspeak.controller.trace;

import java.util.List;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceResponse;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.validation.ValidFile;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.service.TraceService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/traces")
@Validated
public class TraceController {

	private TraceService traceService;

	public TraceController(TraceService traceService) {
		this.traceService = traceService;
	}

	@GetMapping("/my")
	public ResponseEntity<List<Trace>> getMyTraces(@AuthenticationPrincipal Jwt jwt) {
		List<Trace> nearbyTraces = traceService.getAllTraces();
		return ResponseEntity.ok(nearbyTraces);
	}

	@GetMapping("/nearby")
	public ResponseEntity<List<TraceLocationDTO>> getTracesNearby(@RequestParam double longitude,
			@RequestParam double latitude,
			@RequestParam int distance) {
		List<TraceLocationDTO> nearbyTraces = traceService.getNearbyTraces(longitude, latitude, distance);
		return ResponseEntity.ok(nearbyTraces);
	}

	@PostMapping
	public ResponseEntity<TraceResponse> createTrace(@AuthenticationPrincipal Jwt jwt,
			@Valid @ValidFile(required = false, maxSize = 1024 * 1024 * 3, allowedTypes = { "image/jpeg", "image/png",
					"image/jpg" }) @RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart @Valid TraceUploadDTO traceUploadDTO) {
		String userId = jwt.getSubject();
		Trace trace = traceService.createTrace(userId, file, traceUploadDTO);

		TraceResponse traceResponse = new TraceResponse(
				trace.getId(),
				trace.getDescription(),
				trace.getLocation().getX(),
				trace.getLocation().getY(),
				trace.getComments(),
				trace.getTags(),
				// trace.getAuthor(),
				trace.getCreatedAt(),
				trace.getIsActive());
		return ResponseEntity.ok(traceResponse);
	}

	@DeleteMapping("/{traceId}")
	public ResponseEntity<Void> deleteTrace(@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long traceId) {
		traceService.deleteTrace(traceId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{traceId}")
	public ResponseEntity<TraceDownloadDTO> getTraceInfo(@AuthenticationPrincipal Jwt jwt,
			@PathVariable String traceId) {
		String userId = jwt.getSubject();
		TraceDownloadDTO traceInfo = traceService.getTraceInfo(userId, traceId);
		return ResponseEntity.ok(traceInfo);
	}

	@GetMapping("/tags")
	public ResponseEntity<List<Tag>> getTags() {
		List<Tag> tags = traceService.getAllTags();
		return ResponseEntity.ok(tags);
	}

}
