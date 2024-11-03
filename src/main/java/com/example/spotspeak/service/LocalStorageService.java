package com.example.spotspeak.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
@Profile({ "local", "test" })
public class LocalStorageService implements StorageService {

    org.slf4j.Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    @Value("${storage.local.root-location}")
    private String rootLocationString;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(rootLocationString);
            if (!Files.exists(rootLocation)) {
                logger.info("Creating upload directory.");
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            logger.error("Could not initialize storage service.", e);
            throw new RuntimeException("Could not create upload directory.", e);
        }
    }

    @Override
    public void storeFile(MultipartFile file, String key) {
        try {
            Path destinationFile = this.rootLocation.resolve(key);
            logger.info("Trying to store file at: " + destinationFile);

            Path parentDir = destinationFile.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                logger.info("Creating parent directory: " + parentDir);
                Files.createDirectories(parentDir);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.error("Failed to store file.", e);
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            Path file = this.rootLocation.resolve(
                    Paths.get(key).normalize().toAbsolutePath());
            Files.delete(file);
            logger.info("File deleted: " + key);
        } catch (IOException e) {
            logger.error("Failed to delete file.", e);
        }
    }

    @Override
    public void cleanUp() {
        try {
            FileUtils.deleteDirectory(rootLocation.toFile());
        } catch (IOException e) {
            logger.error("Failed to clean up storage.", e);
        }
    }

    @Override
    public String getResourceAccessUrl(String key) {
        return "http://localhost:8080/" + key;
    }

}
