package com.example.spotspeak.service;

import com.example.spotspeak.dto.PresignedUploadUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    private final Duration PRESIGNED_URL_EXPIRATION_DURATION = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public String generatePresignedDownloadUrl(String keyName) {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_EXPIRATION_DURATION)
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toExternalForm();
    }

    public PresignedUploadUrlResponse generatePresignedUploadUrl(String fileName) {
            String uniqueKeyName = generateUniqueKeyName(fileName);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueKeyName)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_EXPIRATION_DURATION)
                    .putObjectRequest(objectRequest)
                    .build();


            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            return new PresignedUploadUrlResponse(presignedRequest.url().toExternalForm(), uniqueKeyName);
    }

    public String generateUniqueKeyName(String fileName) {
        String uniqueId = UUID.randomUUID().toString();

        return "user-uploads/" + uniqueId + "_" + fileName;
    }
}
