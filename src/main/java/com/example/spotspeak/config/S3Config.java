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
        return S3Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create()) // uses .aws/credentials and .aws/config
                .region(Region.EU_CENTRAL_1)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(ProfileCredentialsProvider.create()) // DefaultCredentialsProvider.create() for
                .region(Region.EU_CENTRAL_1)
                .build();
    }
}
