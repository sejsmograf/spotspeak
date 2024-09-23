package com.example.spotspeak.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.service.TraceService;

@RestController
@RequestMapping("/api/traces")
public class TraceController {

	private TraceService traceService;

	public TraceController(TraceService traceService) {
		this.traceService = traceService;
	}

	@GetMapping
	public ResponseEntity<List<Trace>> getAllTraces() {
		return ResponseEntity.ok(traceService.getAllTraces());
	}
}
