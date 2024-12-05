package com.example.spotspeak.dto;

import java.util.UUID;

public record RankingDTO(
    Integer rankNumber,
    UUID friendId,
    String username,
    String firstName,
    String lastName,
    String profilePictureUrl,
    Integer totalPoints
) {
    public RankingDTO withRank(Integer rank) {
        return new RankingDTO(rank, this.friendId, this.username, this.firstName, this.lastName, this.profilePictureUrl, this.totalPoints);
    }

    public RankingDTO withTotalPoints(Integer totalPoints) {
        return new RankingDTO(this.rankNumber, this.friendId, this.username, this.firstName, this.lastName, this.profilePictureUrl, totalPoints);
    }
}
