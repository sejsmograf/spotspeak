package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.spotspeak.exception.PasswordChallengeFailedException;
import com.example.spotspeak.service.PasswordChallengeService.ChallengeEntry;

public class PasswordChallengeServiceTest
        extends BaseServiceIntegrationTest {

    private PasswordChallengeService passwordChallengeService;

    @BeforeEach
    void setUp() {
        passwordChallengeService = new PasswordChallengeService();
    }

    @Test
    public void createAndStoreChallenge_shouldCreateChallenge() {
        UUID issuedFor = UUID.randomUUID();

        String token = passwordChallengeService.createAndStoreChallenge(issuedFor);

        assertThat(token).isNotBlank();
    }

    @Test
    public void verifyChallengeOrThrow_shouldPass_whenTokenStored() {
        UUID randomUUID = UUID.randomUUID();
        String token = passwordChallengeService.createAndStoreChallenge(randomUUID);

        passwordChallengeService.verifyChallengeOrThrow(token, randomUUID);
    }

    @Test
    public void verifyChallengeOrThrow_shouldThrow_whenTokenNotStored() {
        UUID randomUUID = UUID.randomUUID();
        String invalidToken = "invalid";

        assertThrows(PasswordChallengeFailedException.class, () -> {
            passwordChallengeService.verifyChallengeOrThrow(invalidToken, randomUUID);
        });
    }

    @Test
    public void verifyChallengeOrThrow_shouldThrow_whenTokenIssuedForAnotherId() {
        UUID issuedFor = UUID.randomUUID();
        UUID notIssuedFor = UUID.randomUUID();
        String token = passwordChallengeService.createAndStoreChallenge(issuedFor);

        assertThrows(PasswordChallengeFailedException.class,
                () -> passwordChallengeService.verifyChallengeOrThrow(token, notIssuedFor));
    }

    @Test
    public void verifyChallangeOrThrowShouldThrow_whenTokenExpired() {
        UUID issuedFor = UUID.randomUUID();
        String token = passwordChallengeService.createAndStoreChallenge(issuedFor);
        ChallengeEntry expiredEntry = new ChallengeEntry(issuedFor, Instant.now().minusSeconds(10000));
        passwordChallengeService.challenges.put(token, expiredEntry);

        assertThrows(PasswordChallengeFailedException.class,
                () -> passwordChallengeService.verifyChallengeOrThrow(token, issuedFor));
    }

    @Test
    public void cleanExpiredChallenges_shouldRemoveExpiredChallenges() {
        UUID issuedFor = UUID.randomUUID();
        String token = passwordChallengeService.createAndStoreChallenge(issuedFor);
        ChallengeEntry expiredEntry = new ChallengeEntry(issuedFor, Instant.now().minusSeconds(10000));
        passwordChallengeService.challenges.put(token, expiredEntry);

        ReflectionTestUtils.invokeMethod(passwordChallengeService, "cleanExpiredChallenges");
        var entries = passwordChallengeService.challenges;
        assertThat(entries).isEmpty();
    }
}
