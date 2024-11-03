package com.example.spotspeak.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void storeFile(MultipartFile file, String key);

    void deleteFile(String key);

    void cleanUp();

    String getResourceAccessUrl(String key);
}
