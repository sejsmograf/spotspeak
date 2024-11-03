package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.exception.TraceNotWithinDistanceException;
import com.example.spotspeak.repository.TestEntityFactory;

import jakarta.transaction.Transactional;

public class TraceDiscoveryServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    private TraceDiscoveryService traceDiscoveryService;

    private User testUser;
    private Trace testTrace;

    @BeforeEach
    public void setUp() {
        testUser = TestEntityFactory.createPersistedUser(entityManager);
        testTrace = TestEntityFactory.createPersistedTrace(entityManager, testUser, null);
    }

    @Test
    @Transactional
    void discoverTrace_shouldAddDiscoverer_whenWithinDisance() {
        double traceLong = testTrace.getLongitude();
        double traceLat = testTrace.getLatitude();

        Trace discoveredTrace = traceDiscoveryService
                .discoverTrace(testUser, testTrace.getId(), traceLong, traceLat);

        assertThat(discoveredTrace).isNotNull();
        assertThat(testUser.getDiscoveredTraces()).contains(testTrace);
        assertThat(testTrace.getDiscoverers()).contains(testUser);
    }

    @Test
    @Transactional
    void discoverTrace_shouldThrow_whenNotWithinDistance() {
        double traceLong = testTrace.getLongitude();
        double traceLat = testTrace.getLatitude();
        double offsetLong = traceLong + 0.1;

        assertThrows(TraceNotWithinDistanceException.class, () -> traceDiscoveryService
                .discoverTrace(testUser, testTrace.getId(), offsetLong, traceLat));
    }

    @Test
    @Transactional
    void findUserDiscoverTraces_shouldReturnTrace_whenUserDiscoveredIt() {
        double traceLong = testTrace.getLongitude();
        double traceLat = testTrace.getLatitude();

        traceDiscoveryService.discoverTrace(testUser, testTrace.getId(), traceLong, traceLat);
        List<Trace> userDiscoveredTraces = traceDiscoveryService.findUserDisoveredTraces(
                testUser.getId().toString());

        assertThat(userDiscoveredTraces).isNotEmpty();
        assertThat(userDiscoveredTraces).hasSize(1);
        assertThat(userDiscoveredTraces).contains(testTrace);
    }

    @Test
    @Transactional
    void discoverTrace_shouldNotChangeData_whenCalledMultipleTimes() {
        double traceLong = testTrace.getLongitude();
        double traceLat = testTrace.getLatitude();

        traceDiscoveryService.discoverTrace(testUser, testTrace.getId(), traceLong, traceLat);
        List<Trace> tracesAfterOneCall = traceDiscoveryService.findUserDisoveredTraces(
                testUser.getId().toString());

        traceDiscoveryService.discoverTrace(testUser, testTrace.getId(), traceLong, traceLat);
        List<Trace> tracesAfterTwoCalls = traceDiscoveryService.findUserDisoveredTraces(
                testUser.getId().toString());

        assertThat(tracesAfterOneCall).isNotEmpty();
        assertThat(tracesAfterTwoCalls).isNotEmpty();
        assertThat(tracesAfterOneCall).hasSize(1);
        assertThat(tracesAfterTwoCalls).hasSize(1);
        assertThat(testUser.getDiscoveredTraces()).hasSize(1);
        assertThat(testTrace.getDiscoverers()).hasSize(1);
    }

    @Test
    @Transactional
    void discoverTrace_shouldThrow_whenInvalidId() {
        assertThrows(TraceNotFoundException.class,
                () -> traceDiscoveryService.discoverTrace(
                        testUser, testTrace.getId() + 1, 0, 0));
    }

    @Test
    @Transactional
    void hasUserDiscoveredTrace_returnTrue_whenDiscovered() {
        double traceLong = testTrace.getLongitude();
        double traceLat = testTrace.getLatitude();

        traceDiscoveryService.discoverTrace(testUser, testTrace.getId(), traceLong, traceLat);
        boolean discovered = traceDiscoveryService.hasUserDiscoveredTrace(testUser, testTrace);

        assertThat(discovered).isTrue();
    }

    @Test
    @Transactional
    void hasUserDiscoveredTrace_returnFalse_whenNotDiscovered() {
        boolean discovered = traceDiscoveryService.hasUserDiscoveredTrace(testUser, testTrace);

        assertThat(discovered).isFalse();
    }
}
