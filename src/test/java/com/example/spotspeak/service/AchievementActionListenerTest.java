//package com.example.spotspeak.service;
//
//import com.example.spotspeak.dto.TraceUploadDTO;
//import com.example.spotspeak.dto.achievement.UserAchievementDTO;
//import com.example.spotspeak.entity.User;
//import com.example.spotspeak.entity.achievement.Achievement;
//import com.example.spotspeak.entity.achievement.UserAchievement;
//import com.example.spotspeak.entity.enumeration.EEventType;
//import com.example.spotspeak.TestEntityFactory;
//import com.example.spotspeak.service.achievement.AchievementActionListener;
//import com.example.spotspeak.service.achievement.UserAchievementService;
//import com.example.spotspeak.service.achievement.UserActionEvent;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.context.ApplicationEventPublisher;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//@SpringBootTest
//public class AchievementActionListenerTest extends BaseServiceIntegrationTest {
//
//    @Autowired
//    private ApplicationEventPublisher eventPublisher;
//
//    @SpyBean
//    private AchievementActionListener achievementActionListener;
//
//    @Captor
//    private ArgumentCaptor<UserActionEvent> eventCaptor;
//
//    @Autowired
//    private TraceCreationService traceCreationService;
//
//    @Autowired
//    private UserAchievementService userAchievementService;
//
//    private User testUser;
//
//    @BeforeEach
//    public void setup() {
//        testUser = TestEntityFactory.createPersistedUser(entityManager);
//        Achievement firstTraceAchievement = TestEntityFactory.createPersistedAchievement(
//                entityManager,
//                "Dodaj pierwszy ślad",
//                "Dodaj pierwszy ślad",
//                20,
//                EEventType.ADD_TRACE,
//                1,
//                null);
//        TestEntityFactory.createPersistedUserAchievement(entityManager, testUser, firstTraceAchievement, 0, 0, null);
//
//        entityManager.flush();
//    }
//
//    @Test
//    @Transactional
//    public void testH2andleUserActionEvent() {
//        // Create a UserActionEvent with sample data
//        UserActionEvent event = UserActionEvent.builder()
//                .user(testUser)
//                .eventType(EEventType.ADD_TRACE)
//                .timestamp(LocalDateTime.now())
//                .build();
//
//        // Publish the event
//        eventPublisher.publishEvent(event);
//
//        // Capture and verify the event
//        verify(achievementActionListener).onUserActionEvent(eventCaptor.capture());
//
//        // Assert the captured event properties
//        UserActionEvent capturedEvent = eventCaptor.getValue();
//        assertThat(capturedEvent.getUser()).isEqualTo(testUser);
//        assertThat(capturedEvent.getEventType()).isEqualTo(EEventType.ADD_TRACE);
//        assertThat(capturedEvent.getTimestamp()).isNotNull();
//    }
//
//    @Test
//    @Transactional
//    public void testHandleUserActionEvent() {
//        TraceUploadDTO traceUploadDTO = TestEntityFactory.createTraceUploadDTO(null);
//
//        traceCreationService.createAndPersistTrace(testUser, null, traceUploadDTO);
//        entityManager.flush();
//
//        List<UserAchievementDTO> userAchievements = userAchievementService
//                .getUserAchievements(testUser.getId().toString());
//        UserAchievementDTO firstTraceAchievementDTO = userAchievements.stream()
//                .filter(ua -> ua.achievementName().equals("Dodaj pierwszy ślad"))
//                .findFirst()
//                .orElseThrow(() -> new AssertionError("Osiągnięcie 'Dodaj pierwszy ślad' nie zostało znalezione"));
//        entityManager.flush();
//        UserAchievement firstTraceAchievement = entityManager.find(UserAchievement.class,
//                firstTraceAchievementDTO.userAchievementId());
//        entityManager.flush();
//        assertThat(firstTraceAchievement.getQuantityProgress()).isEqualTo(1);
//        assertThat(firstTraceAchievement.getCompletedAt()).isNotNull();
//    }
//
//    @Test
//    @Transactional
//    void shouldEmitUserActionEvent_whenTraceCreated() {
//        TraceUploadDTO traceUploadDTO = TestEntityFactory.createTraceUploadDTO(null);
//        traceCreationService.createAndPersistTrace(testUser, null, traceUploadDTO);
//        flushAndClear();
//
//        verify(achievementActionListener, times(1)).onUserActionEvent(eventCaptor.capture());
//
//        UserActionEvent emittedEvent = eventCaptor.getValue();
//        assertThat(emittedEvent.getUser()).isEqualTo(testUser);
//        assertThat(emittedEvent.getEventType()).isEqualTo(EEventType.ADD_TRACE);
//        // assertThat(emittedEvent.getLocation()).isEqualTo();
//        assertThat(emittedEvent.getTimestamp()).isNotNull();
//    }
//}
