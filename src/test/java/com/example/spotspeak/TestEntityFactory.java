package com.example.spotspeak;

import com.example.spotspeak.dto.CommentRequestDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.entity.*;
import com.example.spotspeak.entity.enumeration.EFriendRequestStatus;
import com.example.spotspeak.entity.enumeration.ETraceType;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.keycloak.representations.idm.UserRepresentation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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

    public static CommentRequestDTO createCommentRequestDTO(String content) {
        return new CommentRequestDTO(content);
    }
}
