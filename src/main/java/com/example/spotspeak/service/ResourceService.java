package com.example.spotspeak.service;

import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.repository.ResourceRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResourceService {

    private ResourceRepository resourceRepository;
    private StorageService storageService;
    private KeyGenerationService keyGenerationService;

    public ResourceService(ResourceRepository resourceRepository,
            StorageService storageService,
            KeyGenerationService keyGenerationService) {
        this.resourceRepository = resourceRepository;
        this.storageService = storageService;
        this.keyGenerationService = keyGenerationService;
    }

    public Resource uploadTraceResource(String userId, MultipartFile file) {
        String key = keyGenerationService.generateUniqueTraceResourceKey(userId, file.getName());

        storageService.storeFile(file, key);

        Resource resource = Resource.builder()
                .key(key)
                .fileType(file.getContentType())
                .build();

        return resourceRepository.save(resource);
    }

    public Resource uploadUserProfilePicture(String userId, MultipartFile file) {
        String key = keyGenerationService.generateUserProfilePictureKey(userId);
        storageService.storeFile(file, key);
        Resource resource = Resource.builder()
                .key(key)
                .fileType(file.getContentType())
                .build();

        return resourceRepository.save(resource);
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId).get();
        resourceRepository.deleteById(resource.getId());
        storageService.deleteFile(resource.getKey());
    }
}
