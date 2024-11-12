package com.example.spotspeak.service;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

import jakarta.transaction.Transactional;

public class TraceExpirationServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    TraceExpirationService traceExpirationService;

    @Test
    @Transactional
    public void deactivateExpiredTraces_shouldDeactivateExpiredTraces() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
        trace.setExpiresAt(LocalDateTime.now().minusDays(10));

        int deactivatedCount = traceExpirationService.deactivateExpiredTraces();
        flushAndClear();

        assertThat(deactivatedCount).isEqualTo(1);
    }

    @Test
    @Transactional
    public void deactivateExpiredTraces_shouldNotDeactivateNonExpiredTraces() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
        trace.setExpiresAt(LocalDateTime.now().plusDays(1));
        flushAndClear();

        int expiredTracesCount = traceExpirationService.deactivateExpiredTraces();
        Trace retrieved = entityManager.find(Trace.class, trace.getId());

        assertThat(expiredTracesCount).isEqualTo(0);
        assertThat(retrieved.getIsActive()).isTrue();
    }
}
