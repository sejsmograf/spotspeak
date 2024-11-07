package com.example.spotspeak.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.spotspeak.exception.PasswordChallengeFailedException;

@Service
public class PasswordChallengeService {

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();
    private final int TOKEN_LENGTH = 64;

    private final int CLEANUP_INTERVAL_MS = 60 * 1000; // 1 minute
    private final int CHALLENGE_EXPIRATION_MS = 60 * 5 * 1000; // 5 minutes

    record ChallengeEntry(UUID userId, Instant issuedAt) {
    }

    ConcurrentMap<String, ChallengeEntry> challenges = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    private void cleanExpiredChallenges() {
        Iterator<ConcurrentMap.Entry<String, ChallengeEntry>> iterator = challenges.entrySet().iterator();

        while (iterator.hasNext()) {
            ConcurrentMap.Entry<String, ChallengeEntry> entry = iterator.next();
            Instant issuedAt = entry.getValue().issuedAt();
            Instant challengeExpiresAt = issuedAt.plusMillis(CHALLENGE_EXPIRATION_MS);

            if (challengeExpiresAt.isBefore(Instant.now())) {
                iterator.remove();
            }
        }
    }

    public void verifyChallengeOrThrow(String token, UUID userId) {
        ChallengeEntry entry = challenges.get(token);

        if (entry == null) {
            throw new PasswordChallengeFailedException("Invalid challenge token");
        }

        if (!entry.userId().equals(userId)) {
            throw new PasswordChallengeFailedException("Challenge token does not match the user");
        }
        Instant challengeExpiresAt = entry.issuedAt().plusMillis(CHALLENGE_EXPIRATION_MS);

        if (challengeExpiresAt.isBefore(Instant.now())) {
            throw new PasswordChallengeFailedException("Challenge token has expired");
        }
    }

    public String createAndStoreChallenge(UUID userId) {
        String token = generateToken();
        challenges.put(token, new ChallengeEntry(userId, Instant.now()));
        return token;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }
}
