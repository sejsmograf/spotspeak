package com.example.spotspeak.service;

public interface KeyGenerationService {
    public String generateUserProfilePictureKey(String userId, String filaName);

    public String generateUniqueTraceResourceKey(String authorId, String fileName);
}
