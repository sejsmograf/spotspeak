package com.example.spotspeak.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

@Service
public class S3KeyGenerationService implements KeyGenerationService {
    private final String USER_UPLOADS_KEY = "user-uploads";
    private final String PROFILE_PICTURES_KEY = "profile-picture";
    private final String TRACE_FILES_KEY = "trace-files";
    private final String ACHIEVEMENT_ICONS_KEY = "achievements-icons";

    @Override
    public String generateUserProfilePictureKey(String userId, String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String filename = PROFILE_PICTURES_KEY + "_" + System.currentTimeMillis() + "." + extension;
        return String.format("%s/%s/%s", USER_UPLOADS_KEY, userId, filename);
    }

    @Override
    public String generateUniqueTraceResourceKey(String authorId, String originalFilename) {
        originalFilename = FilenameUtils.getName(originalFilename);
        long timestamp = System.currentTimeMillis();
        return String.format("%s/%s/%s/%d_%s",
                USER_UPLOADS_KEY,
                authorId,
                TRACE_FILES_KEY,
                timestamp,
                originalFilename);
    }

    @Override
    public String generateAchievementIconKey(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        originalFilename = FilenameUtils.getName(originalFilename);
        String filename = originalFilename + "_" + System.currentTimeMillis() + "." + extension;
        return String.format("%s/%s", ACHIEVEMENT_ICONS_KEY , filename);
    }
}
