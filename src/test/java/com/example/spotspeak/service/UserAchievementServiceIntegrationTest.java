// package com.example.spotspeak.service;
//
// import com.example.spotspeak.dto.TraceUploadDTO;
// import com.example.spotspeak.dto.UserAchievementDTO;
// import com.example.spotspeak.entity.User;
// import com.example.spotspeak.entity.achievements.Achievement;
// import com.example.spotspeak.entity.achievements.UserAchievement;
// import com.example.spotspeak.entity.enumeration.EEventType;
// import com.example.spotspeak.TestEntityFactory;
// import com.example.spotspeak.repository.TraceRepository;
// import com.example.spotspeak.repository.UserAchievementRepository;
// import com.example.spotspeak.service.achievement.UserAchievementService;
// import com.example.spotspeak.service.achievement.UserActionEvent;
// import jakarta.persistence.EntityManager;
// import jakarta.transaction.Transactional;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.transaction.support.TransactionTemplate;
//
// import java.util.List;
//
// import static org.mockito.Mockito.*;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// public class UserAchievementServiceIntegrationTest extends
// BaseServiceIntegrationTest {
//
// @MockBean
// private ApplicationEventPublisher eventPublisher;
//
// @Autowired
// private TraceRepository traceRepository;
//
// @Autowired
// private UserService userService;
//
// @Autowired
// private ResourceService resourceService;
//
// @Autowired
// private TagService tagService;
//
// @Autowired
// private EntityManager entityManager;
//
// private TraceCreationService traceCreationService;
//
// @Autowired
// private UserAchievementService userAchievementService;
//
// @Autowired
// private TransactionTemplate transactionTemplate;
//
// @Autowired
// private UserAchievementRepository userAchievementRepository;
//
// private User user;
//
// @BeforeEach
// void setUp() {
// traceCreationService = new TraceCreationService(traceRepository,
// resourceService, userService, tagService,
// eventPublisher);
// user = TestEntityFactory.createPersistedUser(entityManager);
//
// Achievement firstTraceAchievement =
// TestEntityFactory.createPersistedAchievement(
// entityManager,
// "Dodaj pierwszy ślad",
// "Dodaj pierwszy ślad",
// 20,
// EEventType.ADD_TRACE,
// 1,
// null);
//
// TestEntityFactory.createPersistedUserAchievement(entityManager, user,
// firstTraceAchievement, 0, 0, null);
// flushAndClear();
// }
//
// @Test
// @Transactional
// void shouldEmitUserActionEvent_whenTraceCreated() {
// TraceUploadDTO traceUploadDTO = TestEntityFactory.createTraceUploadDTO(null);
// traceCreationService.createAndPersistTrace(user, null, traceUploadDTO);
// flushAndClear();
//
// ArgumentCaptor<UserActionEvent> eventCaptor =
// ArgumentCaptor.forClass(UserActionEvent.class);
// verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
//
// UserActionEvent emittedEvent = eventCaptor.getValue();
// assertThat(emittedEvent.getUser()).isEqualTo(user);
// assertThat(emittedEvent.getEventType()).isEqualTo(EEventType.ADD_TRACE);
// assertThat(emittedEvent.getTimestamp()).isNotNull();
// }
//
// // @Test
// // @Transactional
// // void shouldHandleUserActionEvent_whenEventPublished() {
// // UserActionEvent userActionEvent = new UserActionEvent(user,
// // EEventType.ADD_TRACE, null, null);
// //
// // eventPublisher.publishEvent(userActionEvent);
// //
// // ArgumentCaptor<UserActionEvent> eventCaptor =
// // ArgumentCaptor.forClass(UserActionEvent.class);
// // verify(achievementActionListener,
// // times(1)).onUserActionEvent(eventCaptor.capture());
// //
// // UserActionEvent capturedEvent = eventCaptor.getValue();
// // assertThat(capturedEvent.getUser()).isEqualTo(userActionEvent.getUser());
// //
// assertThat(capturedEvent.getEventType()).isEqualTo(userActionEvent.getEventType());
// // }
//
// @Test
// @Transactional
// void shouldCompleteAchievement_whenUserAddsFirstTrace() {
// TraceUploadDTO traceUploadDTO = TestEntityFactory.createTraceUploadDTO(null);
//
// traceCreationService.createAndPersistTrace(user, null, traceUploadDTO);
//
// List<UserAchievementDTO> userAchievements =
// userAchievementService.getUserAchievements(user.getId().toString());
//
// UserAchievementDTO firstTraceAchievementDTO = userAchievements.stream()
// .filter(ua -> ua.achievementName().equals("Dodaj pierwszy ślad"))
// .findFirst()
// .orElseThrow(() -> new AssertionError("Osiągnięcie 'Dodaj pierwszy ślad' nie
// zostało znalezione"));
//
// UserAchievement firstTraceAchievement = userAchievementRepository
// .findById(firstTraceAchievementDTO.userAchievementId()).orElseThrow();
//
// assertThat(firstTraceAchievement.getQuantityProgress()).isEqualTo(1);
// assertThat(firstTraceAchievement.getCompletedAt()).isNotNull();
// }
// }
