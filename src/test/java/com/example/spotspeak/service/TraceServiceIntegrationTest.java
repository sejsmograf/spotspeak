package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.exception.TraceNotWithinDistanceException;
import com.example.spotspeak.TestEntityFactory;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

public class TraceServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    private TraceService traceService;

    @Autowired
    private LocalStorageService localStorageService;

    private User userWithTraces;
    private final int USER_TRACES_COUNT = 3;
    private List<Trace> testTraces;

    private User userWithoutTraces;

    @BeforeEach
    public void setUp() {
        testTraces = new ArrayList<>();
        userWithTraces = TestEntityFactory.createPersistedUser(entityManager);
        userWithoutTraces = TestEntityFactory.createPersistedUser(entityManager);

        for (int i = 0; i < USER_TRACES_COUNT; i++) {
            testTraces.add(
                    TestEntityFactory.createPersistedTrace(
                            entityManager, userWithTraces, null));
        }
    }

    @AfterEach
    public void cleanStorage() {
        localStorageService.cleanUp();
    }

    @Test
    @Transactional
    void createTrace_shouldPersist_whenNoFileProvided() {
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);

        String userId = userWithoutTraces.getId().toString();
        Trace created = traceService.createTrace(userId, null, dto);
        Trace retrieved = entityManager.find(Trace.class, created.getId());

        assertThat(retrieved).isNotNull().satisfies(
                r -> {
                    assertThat(r.getId()).isEqualTo(created.getId());
                    assertThat(r.getResource()).isNull();
                    assertThat(r.getAuthor()).isEqualTo(userWithoutTraces);
                });
    }

    @Test
    @Transactional
    void createTrace_shouldPersist_whenFileProvided() {
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/jpg", 1024);

        String userId = userWithoutTraces.getId().toString();
        Trace created = traceService.createTrace(userId, mockFile, dto);
        Trace retrieved = entityManager.find(Trace.class, created.getId());

        assertThat(retrieved).isNotNull().satisfies(
                r -> {
                    assertThat(r.getId()).isEqualTo(created.getId());
                    assertThat(r.getResource()).isNotNull();
                    assertThat(r.getAuthor()).isEqualTo(userWithoutTraces);
                });
    }

    @Test
    @Transactional
    void getTracesForAuthor_shouldReturnEmpty_whenNoUserTraces() {
        String userId = userWithoutTraces.getId().toString();
        List<TraceDownloadDTO> userTraces = traceService.getTracesForAuthor(userId);

        assertThat(userTraces).isEmpty();
    }

    @Test
    @Transactional
    void getTracesForAuthor_shouldReturnTraces_whenUserWithWithTraces() {
        String userId = userWithTraces.getId().toString();
        List<TraceDownloadDTO> userTraces = traceService.getTracesForAuthor(userId);

        assertThat(userTraces).isNotNull().isNotEmpty().hasSize(USER_TRACES_COUNT);
    }

    @Test
    @Transactional
    void getTracesForAuthor_shouldReturnCreatedTrace_afterCreatingTrace() {
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/jpg", 1024);
        Trace created = traceService.createTrace(userWithoutTraces.getId().toString(), mockFile, dto);

        String userId = userWithoutTraces.getId().toString();
        List<TraceDownloadDTO> userTraces = traceService.getTracesForAuthor(userId);

        assertThat(userTraces).isNotNull().isNotEmpty().hasSize(1);
        assertThat(userTraces.get(0).id()).isEqualTo(created.getId());
    }

    @Test
    @Transactional
    void getDiscoverdedTraces_shouldReturnEmpty_whenNoTracesDiscovered() {
        String userId = userWithoutTraces.getId().toString();

        List<TraceDownloadDTO> discoveredTraces = traceService.getDiscoveredTraces(userId);

        assertThat(discoveredTraces).isEmpty();
    }

    @Test
    @Transactional
    void discoverTrace_shouldModifyUserAndTraceReferences_whenSuccess() {
        String userId = userWithoutTraces.getId().toString();
        Trace trace = testTraces.get(0);
        double traceLon = trace.getLongitude();
        double traceLat = trace.getLatitude();

        traceService.discoverTrace(userId, trace.getId(), traceLon, traceLat);

        Set<Trace> userDiscoveredTraces = userWithoutTraces.getDiscoveredTraces();
        Set<User> traceDiscoverers = trace.getDiscoverers();

        assertThat(userDiscoveredTraces).isNotEmpty().hasSize(1).contains(trace);
        assertThat(traceDiscoverers).isNotEmpty().hasSize(1).contains(userWithoutTraces);
    }

    @Test
    @Transactional
    void discoverTrace_shouldThrow_whenOutOfRange() {
        String userId = userWithoutTraces.getId().toString();
        Trace trace = testTraces.get(0);
        double traceLon = trace.getLongitude();
        double traceLat = trace.getLatitude();
        double offsetLon = traceLon + 0.1;

        assertThrows(TraceNotWithinDistanceException.class,
                () -> traceService.discoverTrace(userId, trace.getId(), offsetLon, traceLat));
    }

    @Test
    @Transactional
    void getTraceInfoForUser_shouldThrowForbiddenException_whenNotAuhorNorDiscoverer() {
        String userId = userWithoutTraces.getId().toString();
        Long traceId = testTraces.get(0).getId();

        assertThrows(ForbiddenException.class, () -> traceService.getTraceInfoForUser(userId, traceId));
    }

    @Test
    @Transactional
    void getTraceInfoForUser_shouldThrow_whenTraceIdNotFound() {
        String userId = userWithoutTraces.getId().toString();
        List<Long> traceIds = testTraces.stream().map(t -> t.getId()).toList();
        Random random = new Random();

        Long randomId = random.nextLong();
        while (traceIds.contains(randomId)) {
            randomId = random.nextLong();
        }
        Long finalRandomId = randomId;
        assertThrows(TraceNotFoundException.class, () -> traceService.getTraceInfoForUser(userId, finalRandomId));
    }

    @Test
    @Transactional
    void getTraceInfoForUser_shouldReturnDTO_whenAuthor() {
        String userId = userWithTraces.getId().toString();
        Long traceId = testTraces.get(0).getId();

        TraceDownloadDTO dto = traceService.getTraceInfoForUser(userId, traceId);

        assertThat(dto.id()).isEqualTo(traceId);
    }

    @Test
    @Transactional
    void getTraceInfoForUser_shouldReturnDTO_whenUserHasDiscoveredTrace() {
        String userId = userWithoutTraces.getId().toString();
        Trace trace = testTraces.get(0);
        double traceLon = trace.getLongitude();
        double traceLat = trace.getLatitude();
        Long traceId = trace.getId();

        traceService.discoverTrace(userId, traceId, traceLon, traceLat);
        TraceDownloadDTO dto = traceService.getTraceInfoForUser(userId, traceId);

        assertThat(dto.id()).isEqualTo(traceId);
    }

    @Test
    @Transactional
    void getNearbyTraces_shouldReturnTrace_whenSameLocation() {
        String userId = userWithoutTraces.getId().toString();
        Trace trace = testTraces.get(0);
        double traceLon = trace.getLongitude();
        double traceLat = trace.getLatitude();
        Long traceId = trace.getId();

        List<TraceLocationDTO> nearby = traceService.getNearbyTracesForUser(userId, traceLon, traceLat, 10);
        assertThat(nearby).isNotEmpty();
        assertThat(nearby).anyMatch(dto -> dto.id().equals(traceId));
    }

    @Test
    @Transactional
    void deleteTrace_shouldThrow_whenNonAuthorCall() {
        String nonCreatorId = userWithoutTraces.getId().toString();
        Long traceId = testTraces.get(0).getId();

        assertThrows(ForbiddenException.class, () -> traceService.deleteTrace(traceId, nonCreatorId));
    }

    @Test
    @Transactional
    void deleteTrace_shouldWork_whenTraceHasDiscoverers() {
        String creatorId = userWithTraces.getId().toString();
        String discovererId = userWithoutTraces.getId().toString();
        Trace trace = testTraces.get(0);
        double traceLon = trace.getLongitude();
        double traceLat = trace.getLatitude();
        Long traceId = trace.getId();

        traceService.discoverTrace(discovererId, traceId, traceLon, traceLat);
        traceService.discoverTrace(creatorId, traceId, traceLon, traceLat);

        traceService.deleteTrace(traceId, creatorId);

        Trace retrieved = entityManager.find(Trace.class, traceId);
        assertThat(retrieved).isNull();
    }

}
