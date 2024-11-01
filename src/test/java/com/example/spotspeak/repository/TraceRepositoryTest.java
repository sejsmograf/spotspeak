package com.example.spotspeak.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;

import com.example.spotspeak.TestDataFactory;
import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class TraceRepositoryTest {

	@Autowired
	TraceRepository traceRepository;

	@Autowired
	TagRepository tagRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EntityManager entityManager;

	@Test
	public void givenTraceWithoutAuthor_whenSaved_shouldTrowDataIntegrityViolation() {
		Trace withoutAuthor = TestDataFactory.createTraceWithoutAuthor(0, 0);
		assertThrows(DataIntegrityViolationException.class, () -> {
			traceRepository.save(withoutAuthor);
		});
	}

	@Test
	@Rollback
	public void givenTraceWithAuthor_whenSaved_shouldPersist() {
		User author = createAndSaveUser();
		Trace savedTrace = createAndSaveTraceWithAuthor(author);

		assertNotNull(savedTrace.getId());
	}

	@Test
	@Rollback
	public void givenSavedTrace_whenRetrieved_shouldContainEqualAuthor() {
		User author = createAndSaveUser();
		Trace savedTrace = createAndSaveTraceWithAuthor(author);

		assertNotNull(savedTrace.getAuthor());
		assertEquals(author, savedTrace.getAuthor());
	}

	@Test
	@Rollback
	public void givenSavedTrace_whenRetrievedById_shouldEqualOriginal() {
		User author = createAndSaveUser();
		Trace savedTrace = createAndSaveTraceWithAuthor(author);

		Optional<Trace> retrievedTrace = traceRepository.findById(savedTrace.getId());
		assertTrue(retrievedTrace.isPresent());
		assertEquals(savedTrace, retrievedTrace.get());
	}

	@Test
	@Rollback
	public void givenSavedTrace_whenDeleted_shouldNotBeRetrievable() {
		User author = createAndSaveUser();
		Trace savedTrace = createAndSaveTraceWithAuthor(author);

		traceRepository.deleteById(savedTrace.getId());

		assertFalse(traceRepository.findById(savedTrace.getId()).isPresent());
		assertTrue(userRepository.findById(author.getId()).isPresent());
	}

	@Test
	@Rollback
	public void givenSavedTraceWithTags_whenRetrieved_shouldContainEqualTags() {
		List<Tag> tags = (List<Tag>) TestDataFactory.createTags();
		User author = createAndSaveUser();
		Trace savedTrace = createAndSaveTraceWithTags(author, tags);

		Optional<Trace> retrievedTrace = traceRepository.findById(savedTrace.getId());

		assertTrue(retrievedTrace.isPresent());
		assertEquals(savedTrace, retrievedTrace.get());
		assertNotNull(retrievedTrace.get().getTags());
		assertFalse(retrievedTrace.get().getTags().isEmpty());
		assertEquals(savedTrace.getTags(), retrievedTrace.get().getTags());
		assertEquals(tags, retrievedTrace.get().getTags());
	}

	@Test
	@Rollback
	public void savedTraceWithTags_whenDeleted_shouldNotDeleteTags() {
		List<Tag> tags = (List<Tag>) TestDataFactory.createTags();
		User author = createAndSaveUser();
		Trace savedTrace = createAndSaveTraceWithTags(author, tags);

		tags.stream().forEach(tag -> assertTrue(tagRepository.findById(tag.getId()).isPresent()));

		traceRepository.deleteById(savedTrace.getId());
		entityManager.flush();

		assertTrue(tagRepository.findAll().iterator().hasNext());
		assertFalse(traceRepository.findById(savedTrace.getId()).isPresent());
		for (Tag tag : tags) {
			assertTrue(tagRepository.findById(tag.getId()).isPresent());
		}
	}

	@Test
	@Rollback
	public void savedTraceWithTags_shouldThrowConstraintViolationException_whenTagDeleted() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		List<Tag> savedTags = (List<Tag>) tagRepository.saveAll(TestDataFactory.createTags());
		validTrace.setTags(savedTags);
		traceRepository.save(validTrace);

		savedTags.forEach(tag -> tagRepository.delete(tag));

		assertThrows(ConstraintViolationException.class, () -> {
			entityManager.flush();
		});
	}

	@Test
	@Rollback
	public void savedTraceWithTags_whenTagsCleared_shouldAllowTagsToBeDeleted() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		List<Tag> savedTags = (List<Tag>) tagRepository.saveAll(TestDataFactory.createTags());
		validTrace.setTags(savedTags);
		Trace savedTrace = traceRepository.save(validTrace);
		List<Long> savedTagIds = savedTags.stream().map(Tag::getId).toList();
		savedTrace.getTags().clear();
		savedTagIds.forEach(tagId -> tagRepository.deleteById(tagId));

		assertTrue(savedTrace.getTags().isEmpty());
		assertTrue(tagRepository.findAll().isEmpty());
		assertTrue(traceRepository.findById(savedTrace.getId()).isPresent());
	}

	@Test
	@Rollback
	public void savedTraceWithLocation_whenRetrievedByNearbyLocation_shouldBeRetrievable() {
		double longitude = 0;
		double latitude = 0;
		int searchDistance = 1;
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser, latitude, longitude);
		Trace savedTrace = traceRepository.save(validTrace);

		List<Object[]> nearbyTraces = traceRepository.findNearbyTracesLocations(longitude,
				latitude,
				searchDistance);

		assertFalse(nearbyTraces.isEmpty());
		assertEquals(savedTrace.getId(), nearbyTraces.get(0)[0]);
	}

	private User createAndSaveUser() {
		return userRepository.save(TestDataFactory.createValidUser());
	}

	private Trace createAndSaveTraceWithAuthor(User author) {
		Trace trace = TestDataFactory.createTraceWithAuthor(author);
		return traceRepository.save(trace);
	}

	private Trace createAndSaveTraceWithTags(User author, List<Tag> tags) {
		Trace trace = TestDataFactory.createTraceWithAuthor(author);
		tagRepository.saveAll(tags);
		trace.setTags(tags);
		return traceRepository.save(trace);
	}
}
