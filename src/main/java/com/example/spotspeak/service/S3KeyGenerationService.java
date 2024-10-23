package com.example.spotspeak.service;

import org.springframework.stereotype.Service;

@Service
public class S3KeyGenerationService implements KeyGenerationService {
	private final String USER_UPLOADS_KEY = "user-uploads";
	private final String PROFILE_PICTURES_KEY = "profile-picture";
	private final String TRACE_FILES_KEY = "trace-files";

	@Override
	public String generateUserProfilePictureKey(String userId) {
		return String.format("%s/%s/%s", USER_UPLOADS_KEY, userId, PROFILE_PICTURES_KEY);
	}

	@Override
	public String generateUniqueTraceResourceKey(String authorId, String fileName) {
		long timestamp = System.currentTimeMillis();
		return String.format("%s/%s/%s/%d_%s",
				USER_UPLOADS_KEY,
				authorId,
				TRACE_FILES_KEY,
				timestamp,
				fileName);
	}
}
