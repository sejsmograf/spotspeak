package com.example.spotspeak.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.repository.TraceRepository;

import jakarta.transaction.Transactional;

@Service
public class TraceExpirationService {

    private final TraceRepository traceRepository;
    private Logger logger = LoggerFactory.getLogger(TraceExpirationService.class);

    public TraceExpirationService(TraceRepository traceRepository) {
        this.traceRepository = traceRepository;
    }

    @Scheduled(fixedRate = TraceConstants.EXPIRED_TRACE_CLEANUP_INTERVAL_MS, initialDelay = 1000 * 20)
    @Transactional
    int deactivateExpiredTraces() {
        logger.info("Deactivating expired traces...");
        int deactivatedCount = traceRepository.deactivateExpiredTraces(LocalDateTime.now());
        logger.info("Deactivated " + deactivatedCount + " expired traces.");
        return deactivatedCount;
    }
}
