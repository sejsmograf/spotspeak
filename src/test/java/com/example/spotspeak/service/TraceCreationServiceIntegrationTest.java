package com.example.spotspeak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.TestEntityFactory;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TraceCreationServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    TraceCreationService traceCreationService;

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
    public void createAndPersistTrace_shouldPersist_whenTagsProvided() {
        List<Tag> tags = TestEntityFactory.createPersistedTags(entityManager, 3);
        List<Long> tagIds = tags.stream().map(Tag::getId).toList();
        User author = TestEntityFactory.createPersistedUser(entityManager);
        TraceUploadDTO dto = TestEntityFactory.createTraceUploadDTO(tagIds);

        Trace trace = traceCreationService.createAndPersistTrace(author, null, dto);
        flushAndClear();
        Trace retrieved = entityManager.find(Trace.class, trace.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTags()).containsExactlyInAnyOrderElementsOf(tags);
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
}
