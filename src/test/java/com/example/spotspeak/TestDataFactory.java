package com.example.spotspeak;

import java.util.UUID;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.LocalDateTime;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

public class TestDataFactory {

	private static GeometryFactory geometryFactory = new GeometryFactory();

	public static User createValidUser() {
		User user = User.builder()
				.id(UUID.randomUUID())
				.username("test")
				.email("test@test.co")
				.firstName("test")
				.lastName("user")
				.registeredAt(LocalDateTime.now())
				.build();

		return user;
	}

	public static Trace createTraceWithoutAuthor() {
		Trace trace = Trace.builder()
				.location(geometryFactory.createPoint(new Coordinate(0, 0)))
				.description("Test trace")
				.isActive(true)
				.build();

		return trace;
	}

	public static Trace createTraceWithAuthor(User author) {
		Trace trace = Trace.builder()
				.location(geometryFactory.createPoint(new Coordinate(0, 0)))
				.description("Test trace")
				.isActive(true)
				.author(author)
				.build();

		return trace;
	}

	public static List<Tag> createTags() {
		return List.of(
				Tag.builder().name("tag1").build(),
				Tag.builder().name("tag2").build(),
				Tag.builder().name("tag3").build());
	}
}
