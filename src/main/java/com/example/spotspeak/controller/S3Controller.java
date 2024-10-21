package com.example.spotspeak.controller;

import com.example.spotspeak.dto.PresignedUploadUrlResponse;
import com.example.spotspeak.service.ResourceService;
import com.example.spotspeak.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/presigned-upload-url")
    public ResponseEntity<PresignedUploadUrlResponse> getPresignedUploadUrl(@RequestParam String fileName) {
        PresignedUploadUrlResponse presignedUploadUrlResponse = s3Service.generatePresignedUploadUrl(fileName);
        return ResponseEntity.ok(presignedUploadUrlResponse);
    }

    @GetMapping("/presigned-download-url")
    public ResponseEntity<String> getPresignedDownloadUrl(@RequestParam String keyName) {
        String presignedUrl = s3Service.generatePresignedDownloadUrl(keyName);
        return ResponseEntity.ok(presignedUrl);
    }

}
