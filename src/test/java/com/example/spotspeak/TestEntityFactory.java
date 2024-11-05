package com.example.spotspeak;

import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
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
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Tag tag = Tag.builder().name("tag" + RANDOM.nextInt(10000)).build();
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
                .fileType("fileType")
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

}
