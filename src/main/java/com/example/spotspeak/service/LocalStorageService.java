package com.example.spotspeak.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
@Profile("local")
public class LocalStorageService implements StorageService {

	private final Path rootLocation = Paths.get("user-uploads");

	@PostConstruct
	public void init() {
		try {
			if (!Files.exists(rootLocation)) {
				Files.createDirectories(rootLocation);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not create upload directory.", e);
		}
	}

	@Override
	public void storeFile(MultipartFile file, String key) {
		try {
			Path destinationFile = this.rootLocation.resolve(
					Paths.get(key).normalize().toAbsolutePath());
			System.out.println("STORING FILE AT: " + destinationFile);

			Path parentDir = destinationFile.getParent();
			if (parentDir != null && !Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to store file.", e);
		}
	}

	@Override
	public void deleteFile(String key) {
		try {
			Path file = this.rootLocation.resolve(
					Paths.get(key).normalize().toAbsolutePath());
			Files.delete(file);
		} catch (IOException e) {
			System.out.println("Failed to delete file: " + key);
		}
	}

	@Override
	public String getResourceAccessUrl(String key) {
		return rootLocation.resolve(key).toString();
	}

}
