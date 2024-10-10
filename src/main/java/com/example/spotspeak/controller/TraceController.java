package com.example.spotspeak.controller;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.service.KeycloakAdminClient;
import com.example.spotspeak.service.TraceService;

@RestController
@RequestMapping("/api/traces")
public class TraceController {

	private TraceService traceService;
	private KeycloakAdminClient keycloakAdminClient;

	public TraceController(
			TraceService traceService,
			KeycloakAdminClient keycloakAdminClient) {
		this.traceService = traceService;
		this.keycloakAdminClient = keycloakAdminClient;
	}

	@GetMapping
	public ResponseEntity<List<Trace>> getAllTraces() {
		return ResponseEntity.ok(traceService.getAllTraces());
	}

	@GetMapping("/users")
	public ResponseEntity<List<UserRepresentation>> getUsers() {
		return ResponseEntity.ok(keycloakAdminClient.getUsers());
	}

}
