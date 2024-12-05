package com.example.spotspeak.service;

import com.example.spotspeak.dto.RankingDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RankingServiceIntegrationTest extends BaseServiceIntegrationTest {

    @Autowired
    private RankingService rankingService;

    private User mainUser;
    private User friend1;
    private User friend2;
    private Achievement achievement1;
    private Achievement achievement2;
    private LocalDateTime localDateTime;

    @BeforeEach
    public void setUp() {
        mainUser = TestEntityFactory.createPersistedUser(entityManager);
        friend1 = TestEntityFactory.createPersistedUser(entityManager);
        friend2 = TestEntityFactory.createPersistedUser(entityManager);

        TestEntityFactory.createPersistedFriendship(entityManager, mainUser, friend1);
        TestEntityFactory.createPersistedFriendship(entityManager, mainUser, friend2);

        achievement1 = TestEntityFactory.createPersistedAchievement(
        entityManager,
        "Dodaj pierwszy ślad",
        "Dodaj pierwszy ślad",
        20,
        EEventType.ADD_TRACE,
        1,
        null);

        achievement2 = TestEntityFactory.createPersistedAchievement(
        entityManager,
        "Odkryj ślad",
        "Odkryj ślad",
        30,
        EEventType.DISCOVER_TRACE,
        1,
        null);

        localDateTime = LocalDateTime.of(2024, 11, 20, 10, 30, 10);
        flushAndClear();
    }

    @Nested
    class GetUserRankingTests {

        @Test
        @Transactional
        void shouldReturnRankedListIncludingUserAndFriends() {
            TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null, localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement2, 1, 0, null,localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement1, 1, 0, null,localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend2, achievement2, 1, 0, null,localDateTime);
            flushAndClear();

            List<RankingDTO> ranking = rankingService.getUserRanking(mainUser.getId().toString());

            assertThat(ranking).hasSize(3);
            assertThat(ranking.get(0).username()).isEqualTo(mainUser.getUsername());
            assertThat(ranking.get(1).username()).isEqualTo(friend2.getUsername());
            assertThat(ranking.get(2).username()).isEqualTo(friend1.getUsername());
        }

        @Test
        @Transactional
        void shouldSortAlphabeticallyWhenPointsAreEqual() {
            TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null,localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement1, 1, 0, null,localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend2, achievement1, 1, 0, null,localDateTime);
            flushAndClear();

            List<RankingDTO> ranking = rankingService.getUserRanking(mainUser.getId().toString());

            List<String> sortedUsernames = ranking.stream().map(RankingDTO::username).toList();
            assertThat(sortedUsernames).isSortedAccordingTo(String::compareToIgnoreCase);
        }

        @Test
        @Transactional
        void shouldReturnOnlyUserWhenNoFriendsExist() {
            entityManager.createQuery("DELETE FROM Friendship").executeUpdate();
            flushAndClear();

            List<RankingDTO> ranking = rankingService.getUserRanking(mainUser.getId().toString());

            assertThat(ranking).hasSize(1);
            assertThat(ranking.get(0).username()).isEqualTo(mainUser.getUsername());
            assertThat(ranking.get(0).totalPoints()).isEqualTo(0);
            assertThat(ranking.get(0).rankNumber()).isEqualTo(1);
        }

        @Test
        @Transactional
        void shouldHandleNoPointsForFriends() {
            List<RankingDTO> ranking = rankingService.getUserRanking(mainUser.getId().toString());

            assertThat(ranking).hasSize(3);
            assertThat(ranking).extracting("totalPoints").containsOnly(0);
        }
    }
}

