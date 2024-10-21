package com.example.spotspeak.controller;

import java.util.List;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
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
	private UserProfileService userService;

	public TraceController(TraceService traceService, UserProfileService userService) {
		this.traceService = traceService;
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<List<Trace>> getAllTraces() {
		return ResponseEntity.ok(traceService.getAllTraces());
	}

	@PostMapping
	public ResponseEntity<Trace> createTrace(@AuthenticationPrincipal Jwt jwt,
											 @RequestParam("file") MultipartFile file,
											 @RequestPart("traceData") TraceUploadDTO traceUploadDTO) {
		String userId = jwt.getSubject();
		Trace trace = traceService.createTrace(userId, file, traceUploadDTO);
		return ResponseEntity.ok(trace);
	}

	@GetMapping("/{traceId}")
	public ResponseEntity<TraceDownloadDTO> getTraceInfo(@AuthenticationPrincipal Jwt jwt, @PathVariable String traceId) {
		String userId = jwt.getSubject(); //should I check if user exists?
		TraceDownloadDTO traceInfo = traceService.getTraceInfo(userId, traceId);
		return ResponseEntity.ok(traceInfo);
	}

}
