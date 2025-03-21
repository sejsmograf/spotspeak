package com.example.spotspeak.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
@Profile("remote")
public class S3Service implements StorageService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    private final Duration PRESIGNED_URL_EXPIRATION_DURATION = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public void storeFile(MultipartFile file, String key) {
        putObject(file, key);
    }

    /**
     * This method will just throw an UnsupportedOperationException.
     * Deleting all files from S3 is not a good idea. This method
     * is here just to satisfy the interface.
     */
    @Override
    public void cleanUp() {
        throw new UnsupportedOperationException("Unimplemented method 'cleanUp'");
    }

    @Override
    public void deleteFile(String key) {
        deleteObject(key);
    }

    @Override
    public String getResourceAccessUrl(String key) {
        return generateCloudFrontUrl(key);
    }

    private String generateCloudFrontUrl(String key) {
        return cloudfrontUrl + "/" + key;
    }

    @Override
    public boolean fileExists(String key) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void putObject(MultipartFile file, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to convert file: " + file.getName());
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteObject(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw e;
        }
    }

}
