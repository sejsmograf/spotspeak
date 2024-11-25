 package com.example.spotspeak.service;

 import com.example.spotspeak.entity.User;
 import com.example.spotspeak.entity.achievement.Achievement;
 import com.example.spotspeak.entity.achievement.ConsecutiveDaysCondition;
 import com.example.spotspeak.entity.achievement.LocationCondition;
 import com.example.spotspeak.entity.achievement.TimeCondition;
 import com.example.spotspeak.entity.achievement.UserAchievement;
 import com.example.spotspeak.entity.enumeration.EDateGranularity;
 import com.example.spotspeak.entity.enumeration.EEventType;
 import com.example.spotspeak.TestEntityFactory;
 import com.example.spotspeak.service.achievement.UserActionEvent;
 import jakarta.transaction.Transactional;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Nested;
 import org.junit.jupiter.api.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.context.SpringBootTest;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.transaction.TestTransaction;

 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.LocalTime;
 import java.util.Set;
 import java.util.UUID;

 import static org.assertj.core.api.Assertions.assertThat;

 @SpringBootTest
 public class AchievementActionListenerIntegrationTest extends BaseServiceIntegrationTest {

     @Autowired
     private ApplicationContext applicationContext;

     private User mainUser;
     private UserActionEvent event;

     @BeforeEach
     public void setUp() {
         mainUser = TestEntityFactory.createPersistedUser(entityManager);
         flushAndClear();
     }

     @Nested
     class AchievementListenerExecution {

         @Test
         @Transactional
         public void shouldCompleteAchievement_whenConditionsMet() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.HOUR,
                 null,
                 null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 1, 12, 30)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement).isNotNull();
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldCompleteAchievement_whenNoConditionsPresent() {
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of()
             );
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.now()
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement).isNotNull();
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldNotCompleteAchievement_whenConditionsNotMet() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager, LocalDateTime.of(2024, 1, 1, 12, 0), EDateGranularity.YEAR, null, null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of(timeCondition)
             );
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2023, 1, 1, 11, 30)
             );
             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(0);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }

         @Test
         @Transactional
         public void shouldProgressButNotCompleteAchievement_whenRequiredQuantityIsNotMet() {
             Achievement achievement = TestEntityFactory.createPersistedAchievement(
                 entityManager,
                 "Default Achievement Name" + UUID.randomUUID(),
                 "Default Achievement Description",
                 100,
                 EEventType.ADD_TRACE,
                 2,
                 Set.of()
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievement(
                 entityManager,
                 mainUser,
                 achievement,
                 0,
                 0,
                 null,
                 null
             );
             flushAndClear();

             event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, null
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement).isNotNull();
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }
     }

     @Nested
     class TimeConditionGranularityTests {

         @Test
         @Transactional
         void shouldSatisfyTimeCondition_basedOnMonthGranularity() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.MONTH,
                 null,
                 null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 15, 10, 30)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldSatisfyTimeCondition_basedOnDayGranularity() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.DAY,
                 null,
                 null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 1, 14, 0)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldSatisfyTimeCondition_basedOnMinuteGranularity() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 30),
                 EDateGranularity.MINUTE,
                 null,
                 null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 1, 12, 30)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldSatisfyTimeCondition_basedOnOnlyHourGranularity() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.ONLY_HOUR,
                 null,
                 null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2023, 2, 2, 12, 45)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldSatisfyTimeCondition_basedOnTimeRangeGranularity() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.TIME_RANGE,
                 LocalTime.of(9, 0),
                 LocalTime.of(17, 0)
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 1, 10, 30)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldSatisfyTimeCondition_basedOnTimeRangeGranularityAfterMidnight() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.TIME_RANGE,
                 LocalTime.of(23, 0),
                 LocalTime.of(4, 0)
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 1, 2, 30)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldNotSatisfyTimeCondition_whenEventDatTimeIsNull() {
             TimeCondition timeCondition = TestEntityFactory.createPersistedTimeCondition(
                 entityManager,
                 LocalDateTime.of(2024, 1, 1, 12, 0),
                 EDateGranularity.HOUR,
                 null,
                 null
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(entityManager, mainUser, Set.of(timeCondition));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, null
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(0);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }
     }

     @Nested
     class LocationConditionTests {

         @Test
         @Transactional
         void shouldSatisfyLocationCondition_whenPointIsInsideRegion() {
             LocationCondition locationCondition = TestEntityFactory.createPersistedLocationCondition(
                 entityManager,
                 "POLYGON ((14 49, 24 49, 24 54, 14 54, 14 49))"
             );

             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager,
                 mainUser,
                 Set.of(locationCondition)
             );
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser,
                 EEventType.ADD_TRACE,
                 TestEntityFactory.createPoint(19, 51),
                 LocalDateTime.now()
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldNotCompleteAchievement_whenLocationConditionNotMet() {
             LocationCondition locationCondition = TestEntityFactory.createPersistedLocationCondition(
                 entityManager,
                 "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))"
             );

             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager,
                 mainUser,
                 Set.of(locationCondition)
             );
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser,
                 EEventType.ADD_TRACE,
                 TestEntityFactory.createPoint(50, 50),
                 LocalDateTime.now()
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(0);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }

         @Test
         @Transactional
         void shouldNotSatisfyLocationCondition_whenPointIsNull() {
             LocationCondition locationCondition = TestEntityFactory.createPersistedLocationCondition(
                 entityManager,
                 "POLYGON ((14 49, 24 49, 24 54, 14 54, 14 49))"
             );

             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager,
                 mainUser,
                 Set.of(locationCondition)
             );
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser,
                 EEventType.ADD_TRACE,
                 null,
                 LocalDateTime.now()
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getQuantityProgress()).isEqualTo(0);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }
     }

     @Nested
     class ConsecutiveDaysConditionTests {

         @Test
         @Transactional
         void shouldProgressStreak_whenConsecutiveDaysConditionMet() {
             ConsecutiveDaysCondition condition = TestEntityFactory.createPersistedConsecutiveDaysCondition(
                 entityManager, 3
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of(condition)
             );
             userAchievement.setCurrentStreak(2);
             userAchievement.setLastActionDate(LocalDate.of(2024, 1, 1));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 2, 10, 0)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(3);
             assertThat(updatedAchievement.getCompletedAt()).isNotNull();
         }

         @Test
         @Transactional
         void shouldResetStreak_whenConsecutiveDaysBroken() {
             ConsecutiveDaysCondition condition = TestEntityFactory.createPersistedConsecutiveDaysCondition(
                 entityManager, 3
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of(condition)
             );
             userAchievement.setCurrentStreak(2);
             userAchievement.setLastActionDate(LocalDate.of(2024, 1, 1));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 3, 10, 0)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }

         @Test
         @Transactional
         void shouldStartNewStreak_whenNoPreviousAction() {
             ConsecutiveDaysCondition condition = TestEntityFactory.createPersistedConsecutiveDaysCondition(
                 entityManager, 3
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of(condition)
             );
             userAchievement.setLastActionDate(null);
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 1, 10, 0)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(1);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }

         @Test
         @Transactional
         void shouldNotProgressStreak_whenEventTimestampIsNull() {
             ConsecutiveDaysCondition condition = TestEntityFactory.createPersistedConsecutiveDaysCondition(
                 entityManager, 3
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of(condition)
             );
             userAchievement.setCurrentStreak(2);
             userAchievement.setLastActionDate(LocalDate.of(2024, 1, 1));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, null
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(2);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }

         @Test
         @Transactional
         void shouldProgressButNotCompleteAchievement_whenStreakNotEnough() {
             ConsecutiveDaysCondition condition = TestEntityFactory.createPersistedConsecutiveDaysCondition(
                 entityManager, 5
             );
             UserAchievement userAchievement = TestEntityFactory.createPersistedUserAchievementWithConditions(
                 entityManager, mainUser, Set.of(condition)
             );
             userAchievement.setCurrentStreak(3);
             userAchievement.setLastActionDate(LocalDate.of(2024, 1, 1));
             flushAndClear();

             UserActionEvent event = new UserActionEvent(
                 mainUser, EEventType.ADD_TRACE, null, LocalDateTime.of(2024, 1, 2, 10, 0)
             );

             applicationContext.publishEvent(event);
             TestTransaction.flagForCommit();
             TestTransaction.end();
             TestTransaction.start();

             UserAchievement updatedAchievement = entityManager.find(UserAchievement.class, userAchievement.getId());
             assertThat(updatedAchievement.getCurrentStreak()).isEqualTo(4);
             assertThat(updatedAchievement.getCompletedAt()).isNull();
         }
     }

 }
