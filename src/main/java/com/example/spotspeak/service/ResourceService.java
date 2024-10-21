package com.example.spotspeak.service;

import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.repository.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    private ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource createAndSaveResource(String keyName, String fileName, String fileType) {
        Resource resource = new Resource();
        resource.setS3Key(keyName);
        resource.setFileName(fileName);
        resource.setFileType(fileType);

        return resourceRepository.save(resource);
    }
}
