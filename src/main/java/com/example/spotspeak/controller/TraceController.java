package com.example.spotspeak.controller;

import java.util.List;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.service.TraceService;

@RestController
@RequestMapping("/api/traces")
public class TraceController {

	private TraceService traceService;
	private UserService userService;

	public TraceController(TraceService traceService, UserService userService) {
		this.traceService = traceService;
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<List<Trace>> getAllTraces() {
		return ResponseEntity.ok(traceService.getAllTraces());
	}

	@PostMapping
	public ResponseEntity<Trace> createTrace(@AuthenticationPrincipal Jwt jwt,
			@RequestBody TraceUploadDTO traceUploadDTO) {
		String userId = jwt.getSubject();
		User user = userService.findByIdOrThrow(userId);
		Trace trace = traceService.createTrace(traceUploadDTO, user);

		return ResponseEntity.ok(trace);
	}

	@GetMapping("/{traceId}")
	public ResponseEntity<TraceDownloadDTO> getTraceInfo(@AuthenticationPrincipal Jwt jwt, @PathVariable Long traceId) {
		String userId = jwt.getSubject();
		userService.findByIdOrThrow(userId);
		TraceDownloadDTO traceInfo = traceService.getTraceInfo(traceId);

		return ResponseEntity.ok(traceInfo);
	}

}
