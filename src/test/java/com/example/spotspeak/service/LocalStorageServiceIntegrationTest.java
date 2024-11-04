package com.example.spotspeak.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.TestEntityFactory;

public class LocalStorageServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageService();

        ReflectionTestUtils.setField(storageService, "directory", "test");
    }

    @Test
    void init_createsRootDirectoryIfNotExists() {
        Path expectedPath = Paths.get("src/main/resources/static/test");

        storageService.init();

        assertThat(Files.exists(expectedPath)).isTrue();

        storageService.cleanUp();
    }

    @Test
    void init_throwsException_WhenCannotCreateRootDirectory() throws IOException {
        MockedStatic<Files> mockFiles = mockStatic(Files.class);
        mockFiles.when(() -> Files.createDirectories(any())).thenThrow(new IOException());

        assertThrows(RuntimeException.class, () -> storageService.init());
        mockFiles.close();
    }

    @Test
    void storeFile_shouldStoreFile() {
        Path expectedPath = Paths.get("src/main/resources/static/test/mock");
        MultipartFile file = TestEntityFactory.createMockMultipartFile("mock", 10);
        storageService.init();

        storageService.storeFile(file, "mock");

        assertThat(Files.exists(expectedPath)).isTrue();
        storageService.cleanUp();
    }

    @Test
    @SuppressWarnings("resource")
    void storeFile_shouldThrowWhenFailedToStoreFile() {
        Path expectedPath = Paths.get("src/main/resources/static/test/mock");
        MockedStatic<Files> mockFiles = mockStatic(Files.class);
        mockFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any())).thenThrow(new IOException());
        MultipartFile file = TestEntityFactory.createMockMultipartFile("mock", 10);
        storageService.init();

        assertThrows(RuntimeException.class, () -> storageService.storeFile(file, "mock"));
        assertThat(Files.exists(expectedPath)).isFalse();
        mockFiles.close();
    }

    @Test
    void deleteFile_shouldDeleteFile() {
        Path expectedPath = Paths.get("src/main/resources/static/test/mock");
        MultipartFile file = TestEntityFactory.createMockMultipartFile("mock", 10);
        storageService.init();
        String key = "mock";
        storageService.storeFile(file, key);

        assertThat(Files.exists(expectedPath)).isTrue();
        storageService.deleteFile(key);

        assertThat(Files.exists(expectedPath)).isFalse();
    }

    @Test
    void deleteFile_shouldThrowwhenFailedToDeleteFile() {
        MockedStatic<Files> mockFiles = mockStatic(Files.class);
        mockFiles.when(() -> Files.delete(any())).thenThrow(new IOException());

        assertThrows(RuntimeException.class, () -> storageService.deleteFile("mock"));
        mockFiles.close();
    }
}
