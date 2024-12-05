package com.example.spotspeak.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Resource;

import jakarta.transaction.Transactional;

public class ResourceServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    private ResourceService resourceService;

    @Autowired 
    private LocalStorageService storageService;

    @AfterEach
    void cleanUp() {
        storageService.cleanUp();
    }

    @Test
    @Transactional
    void uploadFileAndSaveResource_shouldSaveResource() {
        MultipartFile file = TestEntityFactory.createMockMultipartFile("mock", 10);
        String key = "mock";

        resourceService.uploadFileAndSaveResource(file, key);

        int count = entityManager.createQuery("SELECT r FROM Resource r").getResultList().size();
        boolean exists = storageService.fileExists(key);
        assertThat(count).isEqualTo(1);
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    void getResourceAccessUrl_shouldReturnResourceAccessUrl() {
        MultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 10);
        String key = "mock.jpg";

        Resource saved = resourceService.uploadFileAndSaveResource(file, key);
        Long resourceId = saved.getId();

        String resourceAccessUrl = resourceService.getResourceAccessUrl(resourceId);
        assertThat(resourceAccessUrl).isEqualTo("http://localhost:8080/test/mock.jpg");
    }

    @Test
    @Transactional
    void getResourceAccessUrl_shouldReturnResourceAccessUrl_whenResourcePassed() {
        MultipartFile file = TestEntityFactory.createMockMultipartFile("image/jpg", 10);
        String key = "mock.jpg";

        Resource saved = resourceService.uploadFileAndSaveResource(file, key);

        String resourceAccessUrl = resourceService.getResourceAccessUrl(saved);
        assertThat(resourceAccessUrl).isEqualTo("http://localhost:8080/test/mock.jpg");
    }

    @Test
    @Transactional
    void deleteResource_shouldDeleteFile() {
        MultipartFile file = TestEntityFactory.createMockMultipartFile("mock", 10);
        String key = "mock";
        Resource saved = resourceService.uploadFileAndSaveResource(file, key);
        int count = entityManager.createQuery("SELECT r FROM Resource r").getResultList().size();
        boolean exists = storageService.fileExists(key);
        assertThat(count).isEqualTo(1);
        assertThat(exists).isTrue();

        resourceService.deleteResource(saved.getId());
        count = entityManager.createQuery("SELECT r FROM Resource r").getResultList().size();
        exists = storageService.fileExists(key);
        assertThat(count).isEqualTo(0);
        assertThat(exists).isFalse();
    }
}
