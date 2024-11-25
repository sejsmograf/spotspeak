package com.example.spotspeak;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.dto.CommentRequestDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.achievement.AchievementUpdateDTO;
import com.example.spotspeak.dto.achievement.AchievementUploadDTO;
import com.example.spotspeak.dto.achievement.ConditionDTO;
import com.example.spotspeak.dto.achievement.ConsecutiveDaysConditionDTO;
import com.example.spotspeak.dto.achievement.LocationConditionDTO;
import com.example.spotspeak.dto.achievement.TimeConditionDTO;
import com.example.spotspeak.entity.*;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.Condition;
import com.example.spotspeak.entity.achievement.ConsecutiveDaysCondition;
import com.example.spotspeak.entity.achievement.LocationCondition;
import com.example.spotspeak.entity.achievement.TimeCondition;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EDateGranularity;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.entity.enumeration.ETraceType;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.keycloak.representations.idm.UserRepresentation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

@Component
public class TestEntityFactory {

    private static final Random RANDOM = new Random();

    private TestEntityFactory() {
    }

    private static GeometryFactory geometryFactory = new GeometryFactory();

    public static User createPersistedUser(EntityManager em) {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user" + RANDOM.nextInt(10000))
                .email("user" + RANDOM.nextInt(10000) + "@example.com")
                .firstName("test")
                .lastName("user")
                .registeredAt(LocalDateTime.now())
                .build();

        em.persist(user);
        return user;
    }

    public static User createdPersistedUser(EntityManager em, UserRepresentation keycloakUser) {
        User user = User.builder()
                .id(UUID.fromString(keycloakUser.getId()))
                .username(keycloakUser.getUsername())
                .email(keycloakUser.getEmail())
                .firstName(keycloakUser.getFirstName())
                .lastName(keycloakUser.getLastName())
                .registeredAt(LocalDateTime.now())
                .build();

        em.persist(user);
        return user;
    }

    public static Trace createPersistedTrace(EntityManager em, User author, List<Tag> tags) {
        double latitude = RANDOM.nextDouble() * 180 - 90;
        double longitude = RANDOM.nextDouble() * 360 - 180;
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Trace trace = Trace.builder()
                .author(author)
                .traceType(ETraceType.TEXTONLY)
                .location(location)
                .description("description")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusHours(TraceConstants.TRACE_EXPIRATION_HOURS))
                .build();

        if (tags != null) {
            tags.forEach(em::persist);
            trace.setTags(new ArrayList<>(tags));
        }

        em.persist(trace);
        return trace;
    }

    public static Trace createPersistedTrace(EntityManager em, User author, List<Tag> tags, double longitude,
            double latitude) {
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        Trace trace = createPersistedTrace(em, author, tags);
        trace.setLocation(location);

        em.persist(trace);
        return trace;
    }

    public static List<Tag> createPersistedTags(EntityManager em, int count) {
        String[] tagNames = { "ornitology", "botany", "geology", "history", "archeology", "zoology", "entomology",
                "ecology", "geography", "anthropology" };
        if (count > tagNames.length) {
            throw new IllegalArgumentException("Too many tags requested");
        }

        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Tag tag = Tag.builder().name(tagNames[i]).build();
            em.persist(tag);
            tags.add(tag);
        }

        return tags;
    }

    public static TraceUploadDTO createTraceUploadDTO(List<Long> tagIds) {
        return new TraceUploadDTO(12., 12., "description", tagIds);
    }

    public static Resource createPersistedResource(EntityManager em) {
        Resource resource = Resource.builder()
                .resourceKey("resource" + RANDOM.nextInt(10000))
                .fileType("image/jpg")
                .fileSize(1000L)
                .build();

        em.persist(resource);
        return resource;
    }

    public static MockMultipartFile createMockMultipartFile(String contentType, int byteSize) {
        byte[] bytes = new byte[byteSize];
        RANDOM.nextBytes(bytes);
        return new MockMultipartFile("file", "file", contentType, bytes);
    }

    public static PasswordUpdateDTO createPasswordUpdateDTO(String oldPassword, String newPassword) {
        return new PasswordUpdateDTO(oldPassword, newPassword);
    }

    public static Friendship createPersistedFriendship(EntityManager em, User userInitiating, User userReceiving) {
        Friendship friendship = Friendship.builder()
                .userInitiating(userInitiating)
                .userReceiving(userReceiving)
                .createdAt(LocalDateTime.now())
                .build();
        em.persist(friendship);
        return friendship;
    }

    public static FriendRequest createPersistedFriendRequest(EntityManager em, User sender, User receiver,
            EFriendRequestStatus status) {
        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(status)
                .sentAt(LocalDateTime.now())
                .build();
        em.persist(friendRequest);
        return friendRequest;
    }

    public static Comment createPersistedComment(EntityManager em, User author, Trace trace, String content) {
        Comment comment = Comment.builder()
                .author(author)
                .trace(trace)
                .content(content)
                .build();

        em.persist(comment);
        return comment;
    }

    public static CommentMention createPersistedCommentMention(EntityManager em, Comment comment, User mentionedUser) {
        CommentMention mention = CommentMention.builder()
                .comment(comment)
                .mentionedUser(mentionedUser)
                .build();

        em.persist(mention);
        return mention;
    }

    public static CommentRequestDTO createCommentRequestDTO(String content, List<UUID> mentions) {
        return new CommentRequestDTO(content, mentions);
    }

    public static Achievement createPersistedAchievement(EntityManager em, String name, String description, int points, EEventType eventType, int requiredQuantity, Set<Condition> conditions) {
        Achievement achievement = Achievement.builder()
            .name(name)
            .description(description)
            .points(points)
            .eventType(eventType)
            .requiredQuantity(requiredQuantity)
            .build();

        if (conditions != null) {
            conditions.forEach(em::persist);
            achievement.setConditions(conditions);
        }

        em.persist(achievement);
        return achievement;
    }

    public static ConsecutiveDaysCondition createPersistedConsecutiveDaysCondition(EntityManager em, int requiredDays) {
        ConsecutiveDaysCondition condition = ConsecutiveDaysCondition.builder()
            .requiredConsecutiveDays(requiredDays)
            .build();
        em.persist(condition);
        return condition;
    }

    public static LocationCondition createPersistedLocationCondition(EntityManager em, String wktPolygon) {
        try {
            WKTReader reader = new WKTReader();
            Polygon polygon = (Polygon) reader.read(wktPolygon);
            polygon.setSRID(4326);

            LocationCondition locationCondition = LocationCondition.builder()
                .region(polygon)
                .build();

            em.persist(locationCondition);
            return locationCondition;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Point createPoint(double x, double y) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(x, y));
        point.setSRID(4326);
        return point;
    }

    public static TimeCondition createPersistedTimeCondition(EntityManager em, LocalDateTime dateTime, EDateGranularity granularity, LocalTime startTime, LocalTime endTime) {
        TimeCondition condition = TimeCondition.builder()
            .requiredDateTime(dateTime)
            .granularity(granularity)
            .startTime(startTime)
            .endTime(endTime)
            .build();
        em.persist(condition);
        return condition;
    }

    public static UserAchievement createPersistedUserAchievement(EntityManager em, User user, Achievement achievement, int progress, int streak, LocalDate lastActionDate, LocalDateTime completedAt) {
        UserAchievement userAchievement = UserAchievement.builder()
            .user(user)
            .achievement(achievement)
            .quantityProgress(progress)
            .currentStreak(streak)
            .lastActionDate(lastActionDate)
            .completedAt(completedAt)
            .build();
        em.persist(userAchievement);
        return userAchievement;
    }

    public static AchievementUploadDTO createAchievementUploadDTO(
        String name,
        String description,
        int points,
        String eventType,
        int requiredQuantity,
        List<ConditionDTO> conditions
    ) {
        return new AchievementUploadDTO(
            name,
            description,
            points,
            eventType,
            requiredQuantity,
            conditions
        );
    }

    public static ConsecutiveDaysConditionDTO createConsecutiveDaysConditionDTO(int requiredDays) {
        return new ConsecutiveDaysConditionDTO(requiredDays);
    }

    public static TimeConditionDTO createTimeConditionDTO(
        LocalDateTime requiredDateTime,
        String granularity,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new TimeConditionDTO(
            requiredDateTime,
            granularity,
            startTime,
            endTime
        );
    }

    public static LocationConditionDTO createLocationConditionDTO(String polygonWKT) {
        return new LocationConditionDTO(polygonWKT);
    }

    public static AchievementUpdateDTO createAchievementUpdateDTO(
        Long id,
        String name,
        String description,
        int points,
        String eventType,
        int requiredQuantity,
        List<ConditionDTO> conditions
    ) {
        return new AchievementUpdateDTO(id, name, description, points, eventType, requiredQuantity, conditions);
    }

    public static void addConditionsToAchievement(EntityManager em, Achievement achievement, List<ConditionDTO> conditions) {
        Achievement managedAchievement = em.contains(achievement) ? achievement : em.merge(achievement);

        if (conditions != null) {
            for (ConditionDTO conditionDTO : conditions) {
                Condition condition = conditionDTO.toCondition();
                em.persist(condition);
                managedAchievement.getConditions().add(condition);
            }
        }
    }

    public static UserAchievement createPersistedUserAchievementWithConditions(
        EntityManager em,
        User user,
        Set<Condition> conditions
    ) {
        Achievement achievement = createPersistedAchievement(
            em,
            "Default Achievement Name" + UUID.randomUUID(),
            "Default Achievement Description",
            100,
            EEventType.ADD_TRACE,
            1,
            conditions
        );

        return createPersistedUserAchievement(
            em,
            user,
            achievement,
            0,
            0,
            null,
            null
        );
    }

    public static UserAchievement createPersistedUserAchievementWithStreak(
        EntityManager em,
        User user,
        int currentStreak,
        LocalDate lastActionDate
    ) {
        Achievement achievement = createPersistedAchievement(
            em,
            "Default Achievement Name" + UUID.randomUUID(),
            "Default Achievement Description",
            100,
            EEventType.ADD_TRACE,
            1,
            null
        );

        return createPersistedUserAchievement(
            em,
            user,
            achievement,
            0,
            currentStreak,
            lastActionDate,
            null
        );
    }
}
