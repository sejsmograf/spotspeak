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

    public ResourceService(ResourceRepository resourceRepository,
            StorageService storageService,
            KeyGenerationService keyGenerationService) {
        this.resourceRepository = resourceRepository;
        this.storageService = storageService;
    }

    public Resource uploadFileAndSaveResource(MultipartFile file, String key) {
        storageService.storeFile(file, key);

        Resource resource = Resource.builder()
                .resourceKey(key)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        return resourceRepository.save(resource);
    }

    public String getResourceAccessUrl(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId).get();
        String resourceAccessUrl = storageService.getResourceAccessUrl(resource.getResourceKey());
        return resourceAccessUrl;
    }

    public String getResourceAccessUrl(Resource resource) {
        String resourceAccessUrl = storageService.getResourceAccessUrl(resource.getResourceKey());
        return resourceAccessUrl;
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId).get();
        resourceRepository.deleteById(resource.getId());

        // unnecessary, should be handled by @PreRemove
        // storageService.deleteFile(resource.getResourceKey());
    }
}
