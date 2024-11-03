package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.TestEntityFactory;

import jakarta.transaction.Transactional;

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
}
