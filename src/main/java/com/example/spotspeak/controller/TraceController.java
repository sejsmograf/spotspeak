package com.example.spotspeak.controller;

import java.util.List;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceResponse;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.service.TraceTagService;
import com.example.spotspeak.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.service.TraceService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/traces")
public class TraceController {

	private TraceService traceService;
	private TraceTagService traceTagService;

	public TraceController(TraceService traceService, TraceTagService traceTagService) {
		this.traceService = traceService;
		this.traceTagService = traceTagService;
	}

	@GetMapping
	public ResponseEntity<List<Trace>> getAllTraces() {
		return ResponseEntity.ok(traceService.getAllTraces());
	}

	@PostMapping
	public ResponseEntity<TraceResponse> createTrace(@AuthenticationPrincipal Jwt jwt,
											 @RequestParam("file") MultipartFile file,
											 @ModelAttribute TraceUploadDTO traceUploadDTO) {
		String userId = jwt.getSubject();
		Trace trace = traceService.createTrace(userId, file, traceUploadDTO);

		TraceResponse traceResponse =  new TraceResponse(
				trace.getId(),
				trace.getDescription(),
				trace.getLocation().getX(),
				trace.getLocation().getY(),
				trace.getComments(),
				traceTagService.getTagsForTrace(trace.getId()),
//				trace.getAuthor(),
				trace.getCreatedAt(),
				trace.getIsActive()
		);
		return ResponseEntity.ok(traceResponse);
	}

	@GetMapping("/{traceId}")
	public ResponseEntity<TraceDownloadDTO> getTraceInfo(@AuthenticationPrincipal Jwt jwt, @PathVariable String traceId) {
		String userId = jwt.getSubject();
		TraceDownloadDTO traceInfo = traceService.getTraceInfo(userId, traceId);
		return ResponseEntity.ok(traceInfo);
	}

}
