package com.example.spotspeak.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

@DataJpaTest
public class TraceRepositoryTest {

	@Autowired
	TraceRepository traceRepository;

	@Autowired
	TagRepository tagRepository;

	@Autowired
	UserRepository userRepository;

	GeometryFactory geometryFactory;

	private Trace testTrace;
	private User testAuthor;

	public TraceRepositoryTest() {
		this.geometryFactory = new GeometryFactory();
	}

	@BeforeEach
	public void setUp() {
		Trace trace = Trace.builder()
				.location(geometryFactory.createPoint(new Coordinate(0, 0)))
				.description("Test trace")
				.isActive(true)
				.build();

		User author = User.builder()
				.id(UUID.randomUUID())
				.username("test")
				.email("test@test.co")
				.firstName("test")
				.lastName("user")
				.registeredAt(LocalDateTime.now())
				.build();

		this.testAuthor = author;
		this.testTrace = trace;
	}

	@Test
	public void testSaveTraceWithoutAuthorFails() {
		assertThrows(DataIntegrityViolationException.class, () -> {
			traceRepository.save(testTrace);
		});
	}

	@Test
	@Rollback
	public void testSaveTraceWithAuthorSucceeds() {
		User savedUser = userRepository.save(testAuthor);
		testTrace.setAuthor(savedUser);
		traceRepository.save(testTrace);

		assertNotNull(testTrace.getId());
		assertEquals(testTrace.getAuthor().getId(), testAuthor.getId());
	}

	@Test
	@Rollback
	public void testSavedTraceEqualsRetrievedTrace() {
		User savedUser = userRepository.save(testAuthor);
		testTrace.setAuthor(savedUser);
		Trace savedTrace = traceRepository.save(testTrace);

		Trace retrievedTrace = traceRepository.findById(savedTrace.getId()).get();
		assertEquals(savedTrace, retrievedTrace);
		assertEquals(savedUser, retrievedTrace.getAuthor());
	}

	@Test
	@Rollback
	public void testDeleteTrace() {
		User savedUser = userRepository.save(testAuthor);
		testTrace.setAuthor(savedUser);
		Trace savedTrace = traceRepository.save(testTrace);

		traceRepository.deleteById(savedTrace.getId());
		assertFalse(traceRepository.findById(savedTrace.getId()).isPresent());
		assertTrue(userRepository.findById(savedUser.getId()).isPresent());
	}

	@Test
	@Rollback
	public void testSaveTraceWithTags() {
		Tag tag = Tag.builder().name("testTag").build();
		Tag savedTag = tagRepository.save(tag);
		testTrace.setAuthor(userRepository.save(testAuthor));
		testTrace.getTags().add(savedTag);

		Trace savedTrace = traceRepository.save(testTrace);
		assertEquals(savedTrace.getTags().size(), 1);
	}
}
