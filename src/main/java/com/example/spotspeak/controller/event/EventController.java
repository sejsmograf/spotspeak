package com.example.spotspeak.controller.event;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.dto.EventLocationDTO;
import com.example.spotspeak.service.EventService;

@RestController
@RequestMapping("/api/events")
@Validated
public class EventController {

    private EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<EventLocationDTO>> getTracesNearby(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam int distance) {
        String userId = jwt.getSubject();
        List<EventLocationDTO> nearbyEvents = eventService.getNearbyEventsForUser(
                userId, longitude, latitude, distance);

        return ResponseEntity.ok(nearbyEvents);
    }
}
