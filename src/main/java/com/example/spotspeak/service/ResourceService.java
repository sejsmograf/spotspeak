package com.example.spotspeak.service;

import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResourceService {

    private ResourceRepository resourceRepository;
    private S3Service s3Service;

    public ResourceService(ResourceRepository resourceRepository,
            S3Service s3Service) {
        this.resourceRepository = resourceRepository;
        this.s3Service = s3Service;
    }

    public Resource createAndSaveResource(String keyName, String fileName, String fileType) {
        Resource resource = new Resource();
        resource.setS3Key(keyName);
        // resource.setFileName(fileName);
        resource.setFileType(fileType);

        return resourceRepository.save(resource);
    }

    public Resource uploadUserProfilePicture(String userId, MultipartFile file) {
        String s3Key = s3Service.generateUserProfilePictureKey(userId);
        s3Service.uploadFile(file, s3Key);

        Resource resource = Resource.builder()
                .s3Key(s3Key)
                .fileType(file.getContentType())
                .build();

        return resourceRepository.save(resource);
    }
}
