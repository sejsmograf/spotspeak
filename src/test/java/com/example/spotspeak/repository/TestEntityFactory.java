package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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

	public static List<Tag> createPersistedTags(EntityManager em, int count) {
		List<Tag> tags = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Tag tag = Tag.builder().name("tag" + RANDOM.nextInt(10000)).build();
			em.persist(tag);
			tags.add(tag);
		}
		return tags;
	}
}
