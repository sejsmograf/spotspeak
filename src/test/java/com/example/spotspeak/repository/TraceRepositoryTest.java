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
	public void saveTrace_withoutAuthor_shouldFail() {
		Trace withoutAuthor = TestDataFactory.createTraceWithoutAuthor(0, 0);
		assertThrows(DataIntegrityViolationException.class, () -> {
			traceRepository.save(withoutAuthor);
		});
	}

	@Test
	@Rollback
	public void saveTrace_withAuthor_shouldSucceed() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		Trace savedTrace = traceRepository.save(validTrace);

		assertNotNull(savedTrace.getId());
	}

	@Test
	@Rollback
	public void savedTraceAuthor_whenRetrievedFromTrace_shouldEqualOriginal() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		Trace savedTrace = traceRepository.save(validTrace);

		assertNotNull(savedTrace.getAuthor());
		assertEquals(savedUser, savedTrace.getAuthor());
	}

	@Test
	@Rollback
	public void savedTrace_whenRetrievedById_shouldEqualOriginal() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		Trace savedTrace = traceRepository.save(validTrace);

		Optional<Trace> retrievedTrace = traceRepository.findById(savedTrace.getId());

		assertTrue(retrievedTrace.isPresent());
		assertEquals(savedTrace, retrievedTrace.get());
	}

	@Test
	@Rollback
	public void savedTrace_whenDeleted_shouldNotBeRetrievable() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		Trace savedTrace = traceRepository.save(validTrace);

		traceRepository.deleteById(savedTrace.getId());

		assertFalse(traceRepository.findById(savedTrace.getId()).isPresent());
		assertTrue(userRepository.findById(savedUser.getId()).isPresent());
	}

	@Test
	@Rollback
	public void savedTraceWithTags_whenRetrieved_shouldContainEqualTags() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		List<Tag> tags = (List<Tag>) tagRepository.saveAll(TestDataFactory.createTags());
		validTrace.setTags(tags);
		Trace savedTrace = traceRepository.save(validTrace);

		Optional<Trace> retrievedTraceOptional = traceRepository.findById(savedTrace.getId());
		entityManager.flush();

		assertTrue(retrievedTraceOptional.isPresent());
		Trace retrievedTrace = retrievedTraceOptional.get();

		assertEquals(savedTrace, retrievedTrace);
		assertNotNull(retrievedTrace.getTags());
		assertFalse(retrievedTrace.getTags().isEmpty());
		assertEquals(savedTrace.getTags(), retrievedTrace.getTags());
	}

	@Test
	@Rollback
	public void savedTraceWithTags_whenDeleted_shouldNotDeleteTags() {
		User savedUser = userRepository.save(TestDataFactory.createValidUser());
		Trace validTrace = TestDataFactory.createTraceWithAuthor(savedUser);
		List<Tag> savedTags = (List<Tag>) tagRepository.saveAll(TestDataFactory.createTags());
		validTrace.setTags(savedTags);
		Trace savedTrace = traceRepository.save(validTrace);

		traceRepository.deleteById(savedTrace.getId());
		entityManager.flush();

		assertTrue(tagRepository.findAll().iterator().hasNext());
		assertFalse(traceRepository.findById(savedTrace.getId()).isPresent());
		for (Tag tag : savedTags) {
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

}
