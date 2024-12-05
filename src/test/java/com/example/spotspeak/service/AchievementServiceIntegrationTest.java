package com.example.spotspeak.service;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.dto.achievement.AchievementUpdateDTO;
import com.example.spotspeak.dto.achievement.AchievementUploadDTO;
import com.example.spotspeak.dto.achievement.ConditionDTO;
import com.example.spotspeak.dto.achievement.ConsecutiveDaysConditionDTO;
import com.example.spotspeak.dto.achievement.LocationConditionDTO;
import com.example.spotspeak.dto.achievement.TimeConditionDTO;
import com.example.spotspeak.dto.achievement.UserAchievementDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.Condition;
import com.example.spotspeak.entity.achievement.ConsecutiveDaysCondition;
import com.example.spotspeak.entity.achievement.LocationCondition;
import com.example.spotspeak.entity.achievement.TimeCondition;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.exception.AchievementExistsException;
import com.example.spotspeak.exception.AchievementNotFoundException;
import com.example.spotspeak.repository.AchievementRepository;
import com.example.spotspeak.repository.UserAchievementRepository;
import com.example.spotspeak.service.achievement.AchievementService;
import com.example.spotspeak.service.achievement.UserAchievementService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AchievementServiceIntegrationTest extends BaseServiceIntegrationTest {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    private User testUser;
    private Achievement existingAchievement;
    private LocalDateTime localDateTime;

    @BeforeEach
    public void setUp() {
        testUser = TestEntityFactory.createPersistedUser(entityManager);
        existingAchievement = TestEntityFactory.createPersistedAchievement(
            entityManager,
            "Existing Achievement",
            "Description",
            100,
            EEventType.ADD_TRACE,
            5,
            null);
        localDateTime = LocalDateTime.of(2024, 11, 20, 10, 30, 10);

        flushAndClear();
    }

    @Nested
    class CreateAchievementTests {

        @Test
        @Transactional
        void shouldCreateAchievement_whenWhenNoFileProvided() {
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Test Achievement",
                "Description",
                100,
                "ADD_TRACE",
                5,
                null
            );

            Achievement achievement = achievementService.createAchievement(null, uploadDTO);

            Achievement retrieved = entityManager.find(Achievement.class, achievement.getId());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved).isEqualTo(achievement);
            assertThat(retrieved.getName()).isEqualTo(achievement.getName());
            assertThat(retrieved.getDescription()).isEqualTo(achievement.getDescription());
            assertThat(retrieved.getPoints()).isEqualTo(achievement.getPoints());
            assertThat(retrieved.getEventType()).isEqualTo(achievement.getEventType());
            assertThat(retrieved.getRequiredQuantity()).isEqualTo(achievement.getRequiredQuantity());
            assertThat(retrieved.getIconUrl()).isNull();
        }

        @Test
        @Transactional
        void shouldCreateAchievement_whenValidDataProvided() {
            MockMultipartFile file = TestEntityFactory.createMockMultipartFile("image/png", 1000);
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Test Achievement",
                "Description",
                100,
                "ADD_TRACE",
                5,
                null
            );

            Achievement achievement = achievementService.createAchievement(file, uploadDTO);
            Achievement retrieved = entityManager.find(Achievement.class, achievement.getId());
            Long uploadedResourceId = retrieved.getIconUrl().getId();
            Resource uploadedResource = entityManager.find(Resource.class, uploadedResourceId);

            assertThat(achievement).isNotNull();
            assertThat(retrieved.getIconUrl()).isNotNull();
            assertThat(uploadedResource).isNotNull();
            assertThat(uploadedResource.getResourceKey()).isEqualTo(retrieved.getIconUrl().getResourceKey());
        }

        @Test
        @Transactional
        void shouldThrowException_whenAchievementAlreadyExists() {
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Existing Achievement",
                "Description",
                100,
                "ADD_TRACE",
                5,
                null
            );

            assertThrows(AchievementExistsException.class, () ->
                achievementService.createAchievement(null, uploadDTO)
            );
        }

        @Test
        @Transactional
        void shouldThrowIllegalArgumentException_whenInvalidEventTypeProvided() {
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Invalid Event Type Achievement",
                "Description",
                50,
                "INVALID_EVENT_TYPE",
                5,
                null
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                achievementService.createAchievement(null, uploadDTO)
            );

            assertThat(exception.getMessage()).contains("Invalid event type");
        }
    }

    @Nested
    class CreateAchievementWithConditionsTests {

        @Test
        @Transactional
        void shouldCreateAchievement_withConsecutiveDaysCondition() {
            ConsecutiveDaysConditionDTO conditionDTO = TestEntityFactory.createConsecutiveDaysConditionDTO(5);
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Consecutive Days Achievement",
                "Complete tasks for consecutive days",
                50,
                "ADD_TRACE",
                10,
                List.of(conditionDTO)
            );

            Achievement achievement = achievementService.createAchievement(null, uploadDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, achievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getConditions()).hasSize(1);

            Condition condition = retrieved.getConditions().iterator().next();
            assertThat(condition).isInstanceOf(ConsecutiveDaysCondition.class);
            assertThat(((ConsecutiveDaysCondition) condition).getRequiredConsecutiveDays()).isEqualTo(5);
        }

        @Test
        @Transactional
        void shouldCreateAchievement_withTimeCondition() {
            TimeConditionDTO conditionDTO = TestEntityFactory.createTimeConditionDTO(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                "DAY",
                LocalTime.of(8, 0),
                LocalTime.of(18, 0)
            );
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Time Condition Achievement",
                "Perform actions within a specific time",
                30,
                "ADD_TRACE",
                15,
                List.of(conditionDTO)
            );

            Achievement achievement = achievementService.createAchievement(null, uploadDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, achievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getConditions()).hasSize(1);

            Condition condition = retrieved.getConditions().iterator().next();
            assertThat(condition).isInstanceOf(TimeCondition.class);
            TimeCondition timeCondition = (TimeCondition) condition;
            assertThat(timeCondition.getRequiredDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
            assertThat(timeCondition.getGranularity()).isEqualTo(EDateGranularity.DAY);
            assertThat(timeCondition.getStartTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(timeCondition.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @Transactional
        void shouldCreateAchievement_withLocationCondition() {
            String polygonWKT = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
            LocationConditionDTO conditionDTO = TestEntityFactory.createLocationConditionDTO(polygonWKT);
            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Location Condition Achievement",
                "Perform actions within a specific region",
                70,
                "ADD_TRACE",
                20,
                List.of(conditionDTO)
            );

            Achievement achievement = achievementService.createAchievement(null, uploadDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, achievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getConditions()).hasSize(1);

            Condition condition = retrieved.getConditions().iterator().next();
            assertThat(condition).isInstanceOf(LocationCondition.class);
            LocationCondition locationCondition = (LocationCondition) condition;
            assertThat(locationCondition.getRegion().toText()).isEqualTo(polygonWKT);
        }

        @Test
        @Transactional
        void shouldCreateAchievement_withMultipleConditions() {
            ConsecutiveDaysConditionDTO consecutiveDaysCondition = TestEntityFactory.createConsecutiveDaysConditionDTO(7);
            TimeConditionDTO timeCondition = TestEntityFactory.createTimeConditionDTO(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                "MONTH",
                null,
                null
            );
            LocationConditionDTO locationCondition = TestEntityFactory.createLocationConditionDTO(
                "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))"
            );

            AchievementUploadDTO uploadDTO = TestEntityFactory.createAchievementUploadDTO(
                "Multiple Conditions Achievement",
                "Achievement with multiple conditions",
                100,
                "ADD_TRACE",
                25,
                List.of(consecutiveDaysCondition, timeCondition, locationCondition)
            );

            Achievement achievement = achievementService.createAchievement(null, uploadDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, achievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getConditions()).hasSize(3);

            List<Class<?>> conditionTypes = retrieved.getConditions().stream()
                .map(condition -> (Class<?>) condition.getClass())
                .collect(Collectors.toList());

            assertThat(conditionTypes).containsExactlyInAnyOrder(
                ConsecutiveDaysCondition.class,
                TimeCondition.class,
                LocationCondition.class
            );
        }
    }

    @Nested
    class UpdateAchievementTests {

        @Test
        @Transactional
        void shouldUpdateAchievementDetails_withoutChangingConditionsOrIcon() {
            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Updated Achievement",
                "Updated Description",
                100,
                "ADD_TRACE",
                20,
                null
            );

            Achievement updatedAchievement = achievementService.updateAchievement(null, updateDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, updatedAchievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("Updated Achievement");
            assertThat(retrieved.getDescription()).isEqualTo("Updated Description");
            assertThat(retrieved.getPoints()).isEqualTo(100);
            assertThat(retrieved.getEventType()).isEqualTo(EEventType.ADD_TRACE);
            assertThat(retrieved.getRequiredQuantity()).isEqualTo(20);
            assertThat(retrieved.getConditions()).isEmpty();
            assertThat(retrieved.getIconUrl()).isNull();
        }

        @Test
        @Transactional
        void shouldUpdateAchievementConditions() {
            List<ConditionDTO> newConditions = List.of(
                TestEntityFactory.createConsecutiveDaysConditionDTO(5),
                TestEntityFactory.createTimeConditionDTO(
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    "DAY",
                    LocalTime.of(8, 0),
                    LocalTime.of(18, 0)
                )
            );
            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Updated Achievement with Conditions",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                newConditions
            );

            Achievement updatedAchievement = achievementService.updateAchievement(null, updateDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, updatedAchievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("Updated Achievement with Conditions");
            assertThat(retrieved.getConditions()).hasSize(2);

            List<Class<?>> conditionTypes = retrieved.getConditions().stream()
                .map(condition -> (Class<?>) condition.getClass())
                .collect(Collectors.toList());

            assertThat(conditionTypes).containsExactlyInAnyOrder(
                ConsecutiveDaysCondition.class,
                TimeCondition.class
            );
        }

        @Test
        @Transactional
        void shouldReplaceAchievementIcon_whenNewFileProvidedAndUpdatedIconExists() {
            MockMultipartFile existingIcon = TestEntityFactory.createMockMultipartFile("image/png", 1000);
            AchievementUpdateDTO updateIconDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Achievement with New Icon",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                null
            );
            Achievement originalAchievement = achievementService.updateAchievement(existingIcon, updateIconDTO);
            flushAndClear();

            MockMultipartFile newFile = TestEntityFactory.createMockMultipartFile("image/png", 2000);
            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Achievement with New Icon",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                null
            );

            Achievement updatedAchievement = achievementService.updateAchievement(newFile, updateDTO);
            flushAndClear();

            Resource deletedIcon = entityManager.find(Resource.class, originalAchievement.getIconUrl().getId());
            assertThat(deletedIcon).isNull();

            Achievement retrieved = entityManager.find(Achievement.class, updatedAchievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getIconUrl()).isNotNull();

            Resource uploadedResource = entityManager.find(Resource.class, retrieved.getIconUrl().getId());
            assertThat(uploadedResource).isNotNull();
            assertThat(uploadedResource.getResourceKey()).isEqualTo(retrieved.getIconUrl().getResourceKey());
        }

        @Test
        @Transactional
        void shouldReplaceAchievementIcon_whenNewFileProvidedAndUpdatedIconNotExists() {
            MockMultipartFile newFile = TestEntityFactory.createMockMultipartFile("image/png", 2000);
            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Achievement with New Icon",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                null
            );

            Achievement updatedAchievement = achievementService.updateAchievement(newFile, updateDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, updatedAchievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getIconUrl()).isNotNull();

            Resource uploadedResource = entityManager.find(Resource.class, retrieved.getIconUrl().getId());
            assertThat(uploadedResource).isNotNull();
            assertThat(uploadedResource.getResourceKey()).isEqualTo(retrieved.getIconUrl().getResourceKey());
        }

        @Test
        @Transactional
        void shouldClearConditions_whenEmptyConditionsProvided() {
            ConsecutiveDaysConditionDTO conditionDTO = TestEntityFactory.createConsecutiveDaysConditionDTO(5);
            TestEntityFactory.addConditionsToAchievement(
                entityManager,
                existingAchievement,
                List.of(conditionDTO)
            );
            flushAndClear();

            List<Long> initialConditionIds = existingAchievement.getConditions().stream()
                .map(Condition::getId)
                .toList();

            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Achievement Without Conditions",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                List.of()
            );

            Achievement updatedAchievement = achievementService.updateAchievement(null, updateDTO);
            flushAndClear();

            Achievement retrieved = entityManager.find(Achievement.class, updatedAchievement.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getConditions()).isEmpty();

            for (Long conditionId : initialConditionIds) {
                Condition oldCondition = entityManager.find(Condition.class, conditionId);
                assertThat(oldCondition).isNull();
            }
        }

        @Test
        @Transactional
        void shouldThrowException_whenAchievementNotFound() {
            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                999L,
                "Non-Existing Achievement",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                null
            );

            assertThrows(AchievementNotFoundException.class, () ->
                achievementService.updateAchievement(null, updateDTO)
            );
        }

        @Test
        @Transactional
        void shouldThrowIllegalArgumentException_whenInvalidEventTypeProvided() {
            AchievementUpdateDTO updateDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Updated Achievement",
                "Updated Description",
                100,
                "INVALID_EVENT_TYPE",
                15,
                null
            );

            assertThrows(IllegalArgumentException.class, () ->
                achievementService.updateAchievement(null, updateDTO)
            );
        }
    }

    @Nested
    class DeleteAchievementTests {
        @Test
        @Transactional
        void shouldDeleteAchievementWithoutIcon() {
            Long achievementId = existingAchievement.getId();
            achievementService.deleteAchievement(existingAchievement.getId());
            flushAndClear();

            Achievement deletedAchievement = entityManager.find(Achievement.class, achievementId);
            assertThat(deletedAchievement).isNull();
        }

        @Test
        @Transactional
        void shouldDeleteAchievementWithIcon() {
            MockMultipartFile existingIcon = TestEntityFactory.createMockMultipartFile("image/png", 1000);
            AchievementUpdateDTO updateIconDTO = TestEntityFactory.createAchievementUpdateDTO(
                existingAchievement.getId(),
                "Achievement with New Icon",
                "Updated Description",
                100,
                "ADD_TRACE",
                15,
                null
            );
            Achievement originalAchievement = achievementService.updateAchievement(existingIcon, updateIconDTO);
            flushAndClear();

            Long achievementId = existingAchievement.getId();

            achievementService.deleteAchievement(existingAchievement.getId());
            flushAndClear();

            Achievement deletedAchievement = entityManager.find(Achievement.class, achievementId);
            assertThat(deletedAchievement).isNull();

            Resource deletedIcon = entityManager.find(Resource.class, originalAchievement.getIconUrl().getId());
            assertThat(deletedIcon).isNull();
        }

        @Test
        @Transactional
        void shouldThrowExceptionWhenDeletingNonExistentAchievement() {
            assertThrows(AchievementNotFoundException.class, () ->
                achievementService.deleteAchievement(999L)
            );
        }
    }

    @Test
    @Transactional
    void shouldReturnTotalPoints_whenUserHasAchievements() {
        TestEntityFactory.createPersistedUserAchievement(entityManager, testUser, existingAchievement, 1, 0, null, localDateTime);
        flushAndClear();

        Integer totalPoints = achievementService.getTotalPointsByUser(testUser);

        assertThat(totalPoints).isEqualTo(100);
    }

    @Test
    @Transactional
    void shouldReturnZeroPoints_whenUserHasNoAchievements() {
        List<UserAchievementDTO> userAchievements = userAchievementService.getUserAchievements(String.valueOf(testUser.getId()));
        assertThat(userAchievements).isEmpty();

        Integer totalPoints = achievementService.getTotalPointsByUser(testUser);

        assertThat(totalPoints).isEqualTo(0);
    }

    @Test
    @Transactional
    void shouldInitializeAchievementsForAllUsers() {
        achievementRepository.deleteAll();
        flushAndClear();

        existingAchievement = TestEntityFactory.createPersistedAchievement(
            entityManager,
            "Existing Achievement",
            "Description",
            100,
            EEventType.ADD_TRACE,
            5,
            null);

        User user1 = TestEntityFactory.createPersistedUser(entityManager);
        User user2 = TestEntityFactory.createPersistedUser(entityManager);
        flushAndClear();

        achievementService.initializeAchievementsForAllUsers(List.of(user1, user2));
        flushAndClear();

        List<UserAchievementDTO> userAchievements1 = userAchievementService.getUserAchievements(String.valueOf(user1.getId()));
        List<UserAchievementDTO> userAchievements2 = userAchievementService.getUserAchievements(String.valueOf(user2.getId()));

        assertThat(userAchievements1).hasSize(1);
        assertThat(userAchievements1.get(0).achievementName()).isEqualTo(existingAchievement.getName());

        assertThat(userAchievements2).hasSize(1);
        assertThat(userAchievements2.get(0).achievementName()).isEqualTo(existingAchievement.getName());
    }

    @Test
    @Transactional
    void shouldDoNothing_whenUserListIsNullOrEmpty() {
        achievementRepository.deleteAll();
        flushAndClear();

        List<Achievement> achievements = achievementService.getAllAchievements();
        assertThat(achievements).isEmpty();

        List<UserAchievement> userAchievements = userAchievementRepository.findAll();
        assertThat(userAchievements).isEmpty();

        achievementService.initializeAchievementsForAllUsers(List.of());
        flushAndClear();

        userAchievements = userAchievementRepository.findAll();
        assertThat(userAchievements).isEmpty();

        achievementService.initializeAchievementsForAllUsers(null);
        flushAndClear();

        userAchievements = userAchievementRepository.findAll();
        assertThat(userAchievements).isEmpty();
    }
}
