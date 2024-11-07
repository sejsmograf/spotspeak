package com.example.spotspeak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        String isEc2Env = System.getenv("EC2_ENV");

        if ("true".equalsIgnoreCase(isEc2Env)) {
            // Running on EC2
            return S3Client.builder()
                    .region(Region.EU_CENTRAL_1)
                    .build();
        } else {
            // Running locally
            return S3Client.builder()
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .region(Region.EU_CENTRAL_1)
                    .build();
        }
    }

    @Bean
    public S3Presigner s3Presigner() {
        String isEc2Env = System.getenv("EC2_ENV");

        if ("true".equalsIgnoreCase(isEc2Env)) {
            // Running on EC2
            return S3Presigner.builder()
                    .region(Region.EU_CENTRAL_1)
                    .build();
        } else {
            // Running locally
            return S3Presigner.builder()
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .region(Region.EU_CENTRAL_1)
                    .build();
        }
    }
}
