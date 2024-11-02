package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TraceRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TraceRepository traceRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class BasicTraceOperationsTests {
        @Test
        void givenTraceWithoutAuthor_shouldThrowDataIntegrityViolation() {
            Trace withoutAuthor = Trace.builder().description("description").build();

            assertThrows(
                    DataIntegrityViolationException.class, () -> traceRepository.save(withoutAuthor));
        }

        @Test
        void givenTraceWithAuthor_shouldPersist() {
            User author = TestEntityFactory.createPersistedUser(entityManager);

            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            flushAndClear();

            assertThat(trace.getId()).isNotNull();
        }

        @Test
        void givenSavedTrace_shouldContainCorrectAuthor() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            flushAndClear();

            Trace found = traceRepository.findById(trace.getId()).orElseThrow();

            assertThat(found).extracting(Trace::getAuthor).isEqualTo(author);
        }

        @Test
        void whenTraceDeleted_shouldNotDeleteAuthor() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            flushAndClear();

            traceRepository.deleteById(trace.getId());
            flushAndClear();

            assertThat(traceRepository.findById(trace.getId())).isEmpty();
            assertThat(userRepository.findById(author.getId())).isPresent();
        }
    }

    @Nested
    class TraceWithTagsTests {
        @Test
        void whenRetrieved_shouldContainCorrectTags() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            List<Tag> tags = TestEntityFactory.createPersistedTags(entityManager, 3);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, tags);
            flushAndClear();

            Trace found = traceRepository.findById(trace.getId()).orElseThrow();

            assertThat(found.getTags()).isNotEmpty().containsExactlyInAnyOrderElementsOf(tags);
        }

        @Test
        void whenTraceDeleted_shouldNotDeleteTags() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            List<Tag> tags = TestEntityFactory.createPersistedTags(entityManager, 3);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, tags);
            List<Long> tagIds = tags.stream().map(Tag::getId).toList();
            flushAndClear();

            traceRepository.deleteById(trace.getId());
            flushAndClear();

            assertThat(traceRepository.findById(trace.getId())).isEmpty();
            assertThat(tagRepository.findAllById(tagIds))
                    .hasSize(tags.size())
                    .containsExactlyInAnyOrderElementsOf(tags);
        }

        @Test
        void whenTagsDeleted_shouldThrowConstraintViolation() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            List<Tag> tags = TestEntityFactory.createPersistedTags(entityManager, 3);
            TestEntityFactory.createPersistedTrace(entityManager, author, tags);
            flushAndClear();

            tags.forEach(tag -> tagRepository.delete(tag));
            assertThrows(ConstraintViolationException.class, () -> entityManager.flush());
        }

        @Test
        void whenTagsCleared_shouldAllowTagDeletion() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            List<Tag> tags = TestEntityFactory.createPersistedTags(entityManager, 3);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, tags);
            flushAndClear();

            Trace persistedTrace = traceRepository.findById(trace.getId()).orElseThrow();
            persistedTrace.setTags(new ArrayList<>());
            traceRepository.save(persistedTrace);
            flushAndClear();

            tags.forEach(tag -> tagRepository.delete(tag));
            flushAndClear();

            assertThat(persistedTrace.getTags()).isEmpty();
            assertThat(tagRepository.findAll()).isEmpty();
            assertThat(traceRepository.findById(trace.getId())).isPresent();
        }
    }

    @Nested
    class LocationBasedTests {
        @Test
        void whenSearchingNearby_shouldFindTraceInRange() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author, null);
            double longitude = trace.getLongitude();
            double latitude = trace.getLatitude();
            double searchDistance = 1;
            flushAndClear();

            List<Object[]> nearbyTraces = traceRepository.findNearbyTracesLocations(longitude, latitude,
                    searchDistance);

            assertThat(nearbyTraces).isNotEmpty().hasSize(1);
            assertThat(nearbyTraces.get(0)[0]).isEqualTo(trace.getId());
        }
    }
}
