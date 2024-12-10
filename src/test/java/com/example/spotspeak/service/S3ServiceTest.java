package com.example.spotspeak.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.spotspeak.TestEntityFactory;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @Test
    void storeFile_shouldUploadFile() {
        MultipartFile mockFile = TestEntityFactory.createMockMultipartFile("image/jpg", 10);

        s3Service.storeFile(mockFile, "test-key");

        Mockito.verify(s3Client).putObject(
                Mockito.any(PutObjectRequest.class),
                Mockito.any(RequestBody.class));
    }

    @Test
    void storeFile_shouldThrowResponseStatusException_whenIOException() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(
                mockFile.getBytes())
                .thenThrow(IOException.class);

        assertThrows(ResponseStatusException.class,
                () -> s3Service.storeFile(mockFile, "test-key"));
    }

    @Test
    void fileExists_shouldReturnTrueWhenExists() {
        Mockito.when(s3Client.headObject(Mockito.any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        boolean exists = s3Service.fileExists("test-key");

        assertThat(exists).isTrue();
    }

    @Test
    void fileExists_shouldReturnFalseWhenException() {
        Mockito.when(s3Client.headObject(Mockito.any(HeadObjectRequest.class)))
                .thenThrow(RuntimeException.class);

        boolean exists = s3Service.fileExists("test-key");
        assertThat(exists).isFalse();
    }

    @Test
    void fileExists_shouldReturnFalseWhenResponseNull() {
        Mockito.when(s3Client.headObject(Mockito.any(HeadObjectRequest.class)))
                .thenReturn(null);

        boolean exists = s3Service.fileExists("test-key");
        assertThat(exists).isFalse();
    }

    @Test
    void getResourceAccessUrl_shouldReturnString() {
        String key = "test-file";

        String resourceAccessUrl = s3Service.getResourceAccessUrl(key);

        assertThat(resourceAccessUrl).isNotNull();
    }

    @Test
    void cleanUp_shouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            s3Service.cleanUp();
        });
    }

    @Test
    void deleteFile_shouldDeleteObject() {
        String key = "test-file";

        s3Service.deleteFile(key);
    }

    @Test
    void deleteFile_shouldThrow_whenFailed() {
        Mockito.when(s3Client.deleteObject(Mockito.any(DeleteObjectRequest.class)))
                .thenThrow(RuntimeException.class);
        String key = "test-file";
        assertThrows(RuntimeException.class, () -> s3Service.deleteFile(key));
    }
}
