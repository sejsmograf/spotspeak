package com.example.spotspeak.service;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDetailsDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.TimeCondition;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.exception.UserAchievementNotFoundException;
import com.example.spotspeak.service.achievement.UserAchievementService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserAchievementServiceIntegrationTest extends BaseServiceIntegrationTest{

    @Autowired
    private UserAchievementService userAchievementService;

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
    class GetUserAchievementsTests {

        @Test
        @Transactional
        void shouldReturnUserAchievementsList_whenUserStartedAchievements() {
            UserAchievement userAchievement1 = TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null, localDateTime);
            UserAchievement userAchievement2 = TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement2, 1, 0, null, localDateTime);
            flushAndClear();

            List<UserAchievementDTO> userAchievementDTOList = userAchievementService.getUserAchievements(String.valueOf(mainUser.getId()));

            assertThat(userAchievementDTOList).hasSize(2)
                .extracting("userAchievementId")
                .containsExactlyInAnyOrder(userAchievement1.getId(), userAchievement2.getId());
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenUserNotStartedAnyAchievements() {
            List<UserAchievementDTO> userAchievementDTOList = userAchievementService.getUserAchievements(String.valueOf(mainUser.getId()));

            assertThat(userAchievementDTOList).isEmpty();
        }

        @Test
        @Transactional
        void shouldReturnResourceUrlInAchievementDTO_whenResourceExists() {
            Resource resource = TestEntityFactory.createPersistedResource(entityManager);
            achievement1.setIconUrl(resource);
            entityManager.merge(achievement1);

            TestEntityFactory.createPersistedUserAchievement(
                entityManager,
                mainUser,
                achievement1,
                1,
                0,
                null,
                localDateTime
            );
            flushAndClear();

            List<UserAchievementDTO> userAchievements = userAchievementService.getUserAchievements(String.valueOf(mainUser.getId()));

            assertThat(userAchievements).hasSize(1);
            assertThat(userAchievements.get(0).resourceAccessUrl()).isNotNull();
            assertThat(userAchievements.get(0).resourceAccessUrl())
                .contains(String.valueOf(resource.getResourceKey()));
        }
    }

    @Nested
    class GetUserAchievementsDetails {

        @Test
        @Transactional
        void shouldReturnUserAchievementDetail_whenUserAchievementExists() {
            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null, localDateTime);
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.userAchievementId()).isEqualTo(userAchievement.getId());
            assertThat(userAchievementDetailsDTO.achievementName()).isEqualTo(userAchievement.getAchievement().getName());
            assertThat(userAchievementDetailsDTO.achievementDescription()).isEqualTo(userAchievement.getAchievement().getDescription());
            assertThat(userAchievementDetailsDTO.points()).isEqualTo(userAchievement.getAchievement().getPoints());
            assertThat(userAchievementDetailsDTO.requiredQuantity()).isEqualTo(userAchievement.getAchievement().getRequiredQuantity());
            assertThat(userAchievementDetailsDTO.quantityProgress()).isEqualTo(userAchievement.getQuantityProgress());
            assertThat(userAchievementDetailsDTO.currentStreak()).isEqualTo(userAchievement.getCurrentStreak());
            assertThat(userAchievementDetailsDTO.completedAt()).isEqualTo(userAchievement.getCompletedAt());
        }

        @Test
        @Transactional
        void shouldThrowException_whenUserAchievementDoesNotExists() {
            assertThrows(UserAchievementNotFoundException.class,
                () -> userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), 999L));
        }

        @Test
        @Transactional
        void shouldReturnResourceUrlInDetails_whenResourceExists() {
            Resource resource = TestEntityFactory.createPersistedResource(entityManager);
            achievement1.setIconUrl(resource);
            entityManager.merge(achievement1);
            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievement(
                entityManager,
                mainUser,
                achievement1,
                1,
                0,
                null,
                localDateTime
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.resourceAccessUrl()).isNotNull();
            assertThat(userAchievementDetailsDTO.resourceAccessUrl())
                .contains(String.valueOf(resource.getResourceKey()));
        }
    }

    @Nested
    class GetFriendsWhoCompletedAchievement {

        @Test
        @Transactional
        void shouldReturnFriendsProfiles_whenFriendsWhoCompletedAchievementExists() {
            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null, localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement1, 1, 0, null, localDateTime);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend2, achievement1, 1, 0, null, localDateTime);
            flushAndClear();

            List<PublicUserProfileDTO> publicUserProfileDTOList =
                userAchievementService.getFriendsWhoCompletedAchievement(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(publicUserProfileDTOList)
                .hasSize(2)
                .extracting("id")
                .containsExactlyInAnyOrder(friend1.getId(), friend2.getId());
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenFriendsWhoCompletedAchievementDoesNotExists() {
            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievement(entityManager, mainUser, achievement1, 1, 0, null, localDateTime);
            flushAndClear();

            List<PublicUserProfileDTO> publicUserProfileDTOList =
                userAchievementService.getFriendsWhoCompletedAchievement(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(publicUserProfileDTOList).isEmpty();
        }

        @Test
        @Transactional
        void shouldThrowException_whenUserAchievementDoesNotExists() {
            assertThrows(UserAchievementNotFoundException.class,
                () -> userAchievementService.getFriendsWhoCompletedAchievement(String.valueOf(mainUser.getId()), 999L));
        }
    }

    @Nested
    class GetCompletedAchievementsByUser {

        @Test
        @Transactional
        void shouldReturnUserCompletedAchievements_whenUserCompletedAchievementsExists() {
            UserAchievement userAchievement1 = TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement1, 1, 0, null, localDateTime);
            UserAchievement userAchievement2 = TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement2, 1, 0, null, localDateTime);
            flushAndClear();

            List<UserAchievementDTO> userAchievementDTOList =
                userAchievementService.getCompletedAchievementsByUser(String.valueOf(mainUser.getId()), friend1.getId());

            assertThat(userAchievementDTOList)
                .hasSize(2)
                .extracting("userAchievementId")
                .containsExactlyInAnyOrder(userAchievement1.getId(), userAchievement2.getId());

            assertThat(userAchievementDTOList)
                .extracting("completedAt")
                .containsOnly(localDateTime);
        }

        @Test
        @Transactional
        void shouldReturnEmptyList_whenUserCompletedAchievementsDoesNotExists() {
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement1, 0, 0, null, null);
            TestEntityFactory.createPersistedUserAchievement(entityManager, friend1, achievement2, 0, 0, null, null);
            flushAndClear();

            List<UserAchievementDTO> userAchievementDTOList =
                userAchievementService.getCompletedAchievementsByUser(String.valueOf(mainUser.getId()), friend1.getId());

            assertThat(userAchievementDTOList).isEmpty();
        }
    }

    @Nested
    class CalculateEndTime {
        @Test
        @Transactional
        void shouldReturnEndTimeForYearGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                EDateGranularity.YEAR,
                null,
                null
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isEqualTo(
                LocalDateTime.of(2024, 12, 31, 23, 59, 59)
            );
        }

        @Test
        @Transactional
        void shouldReturnNullForOnlyHourGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                EDateGranularity.ONLY_HOUR,
                null,
                null
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isNull();
        }

        @Test
        @Transactional
        void shouldReturnEndTimeForMonthGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 2, 1, 0, 0),
                EDateGranularity.MONTH,
                null,
                null
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isEqualTo(
                LocalDateTime.of(2024, 2, 29, 23, 59, 59)
            );
        }

        @Test
        @Transactional
        void shouldReturnEndTimeForDayGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 2, 28, 0, 0),
                EDateGranularity.DAY,
                null,
                null
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isEqualTo(
                LocalDateTime.of(2024, 2, 28, 23, 59, 59)
            );
        }

        @Test
        @Transactional
        void shouldReturnEndTimeForHourGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 2, 28, 14, 0),
                EDateGranularity.HOUR,
                null,
                null
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isEqualTo(
                LocalDateTime.of(2024, 2, 28, 14, 59, 59)
            );
        }

        @Test
        @Transactional
        void shouldReturnEndTimeForMinuteGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 2, 28, 14, 15),
                EDateGranularity.MINUTE,
                null,
                null
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isEqualTo(
                LocalDateTime.of(2024, 2, 28, 14, 15, 59)
            );
        }

        @Test
        @Transactional
        void shouldReturnNullForTimeRangeGranularity() {
            TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                entityManager,
                LocalDateTime.of(2024, 2, 28, 14, 15),
                EDateGranularity.TIME_RANGE,
                LocalTime.of(14, 0),
                LocalTime.of(16, 0)
            );

            UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                entityManager,
                mainUser,
                Set.of(timeCondition)
            );
            flushAndClear();

            UserAchievementDetailsDTO userAchievementDetailsDTO =
                userAchievementService.getUserAchievementDetails(String.valueOf(mainUser.getId()), userAchievement.getId());

            assertThat(userAchievementDetailsDTO.endTime()).isNull();
        }
    }
}
