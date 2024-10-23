package com.example.spotspeak.service;

public interface KeyGenerationService {
	public String generateUserProfilePictureKey(String userId);

	public String generateUniqueTraceResourceKey(String authorId, String fileName);
}
