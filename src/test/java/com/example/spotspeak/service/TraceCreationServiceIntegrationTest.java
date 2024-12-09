package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.enumeration.ETraceType;

import jakarta.transaction.Transactional;

public class TraceCreationServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    TraceCreationService traceCreationService;

    @Autowired
    private StorageService storageService;

    @AfterEach
    public void cleanStorage() {
        storageService.cleanUp();
    }

    @Test
    @Transactional
    public void createAndPersistTrace_shouldPersist_whenNoFileProvided() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);

        Trace trace = traceCreationService.createAndPersistTrace(author, null, dto);

        Trace retrieved = entityManager.find(Trace.class, trace.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getAuthor()).isEqualTo(author);
        assertThat(retrieved.getResource()).isNull();
        assertThat(retrieved).isEqualTo(trace);
    }

    @Test
    @Transactional
    public void createAndPersistTrace_shouldPersistAndUploadFile_whenFileProvided() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);
        MultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 1000);

        Trace trace = traceCreationService.createAndPersistTrace(author, file, dto);
        Trace retrieved = entityManager.find(Trace.class, trace.getId());
        Long uploadedResourceId = retrieved.getResource().getId();
        Resource uploadedResource = entityManager.find(Resource.class, uploadedResourceId);

        assertThat(retrieved.getAuthor()).isEqualTo(author);
        assertThat(retrieved.getResource()).isNotNull();
        assertThat(uploadedResource).isNotNull();
        assertThat(uploadedResource.getResourceKey()).isEqualTo(retrieved.getResource().getResourceKey());
    }

    @Test
    @Transactional
    public void createAndPersistTrace_shouldInferTraceType_whenFileTypeStartsWithImage() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/jpg", 1000);

        Trace trace = traceCreationService.createAndPersistTrace(author, mockFile, dto);
        Trace retrieved = entityManager.find(Trace.class, trace.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTraceType()).isEqualTo(ETraceType.PHOTO);
    }

    @Test
    @Transactional
    public void createAndPersistTrace_shouldInferTraceType_whenFileTypeStartsWithVideo() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("video/mp4", 1000);

        Trace trace = traceCreationService.createAndPersistTrace(author, mockFile, dto);
        Trace retrieved = entityManager.find(Trace.class, trace.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTraceType()).isEqualTo(ETraceType.VIDEO);
    }

    @Test
    @Transactional
    public void createAndPersistTrace_shouldInferTraceType_whenNoFileProvided() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);

        Trace trace = traceCreationService.createAndPersistTrace(author, null, dto);
        Trace retrieved = entityManager.find(Trace.class, trace.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTraceType()).isEqualTo(ETraceType.TEXTONLY);
    }

    @Test
    @Transactional
    public void createAndPersistTrace_shouldInferTraceType_whenInvalidFileType() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(null);
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("application/pdf", 1000);

        assertThrows(IllegalArgumentException.class,
                () -> traceCreationService.createAndPersistTrace(author, mockFile, dto));
    }

}
