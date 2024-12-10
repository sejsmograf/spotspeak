package com.example.spotspeak.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class TraceRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TraceRepository traceRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class BasicTraceOperationsTests {
        @Test
        void saveTrace_shouldThrowException_whenAuthorIsNull() {
            Trace withoutAuthor = Trace.builder().description("description").build();

            assertThrows(
                    DataIntegrityViolationException.class, () -> traceRepository.save(withoutAuthor));
        }

        @Test
        void saveTrace_shouldPersist_whenAuthorProvided() {
            User author = TestEntityFactory.createPersistedUser(entityManager);

            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author);
            flushAndClear();

            assertThat(trace.getId()).isNotNull();
        }

        @Test
        void findTraceById_shouldReturnCorrectActor() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author);
            flushAndClear();

            Trace found = traceRepository.findById(trace.getId()).orElseThrow();

            assertThat(found.getAuthor()).isEqualTo(author);
        }

        @Test
        void deleteTrace_shouldNotDeleteAuthor() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author);
            flushAndClear();

            traceRepository.deleteById(trace.getId());
            flushAndClear();

            assertThat(traceRepository.findById(trace.getId())).isEmpty();
            assertThat(userRepository.findById(author.getId())).isPresent();
        }
    }

    @Nested
    class LocationBasedTests {

        static final double TEST_LONGITUDE_1 = 51.11526452614028; // Wrocław, Benedyktyńska 3
        static final double TEST_LATITUDE_1 = 17.05178471002032;

        static final double TEST_LONGITUDE_2 = 51.11002925078812; // Wrocław, PWr D-2
        static final double TEST_LATITUDE_2 = 17.05735341428798;

        static final double TEST_LONGITUDE_3 = 51.112068609615704; // Wrocław, Pasaż Grunwaldzki
        static final double TEST_LATITUDE_3 = 17.058937191520055;

        @Test
        void findTracesNearby_shouldReturnTrace_whenLocationMatches() {
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace = TestEntityFactory.createPersistedTrace(entityManager, author);
            double tracaLon = trace.getLongitude();
            double traceLat = trace.getLatitude();
            int searchDistanceMeters = 1;
            flushAndClear();

            List<Object[]> nearbyTraces = traceRepository.findNearbyTracesLocationsRaw(tracaLon, traceLat,
                    searchDistanceMeters);

            assertThat(nearbyTraces).isNotEmpty().hasSize(1);
            assertThat(nearbyTraces.get(0)[0]).isEqualTo(trace.getId());
        }

        @Test
        void findTracesNearby_shouldReturnEmpty_whenTraceSlighlyOutsideRange() {
            double tracaLonitude = TEST_LONGITUDE_1;
            double traceLatitude = TEST_LATITUDE_1;

            User author = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedTrace(entityManager, author, tracaLonitude, traceLatitude);
            double searchLongitude = TEST_LONGITUDE_3;
            double searchLatitude = TEST_LATITUDE_3; // slightly outside of range
            int searchDistanceMeters = 500;
            flushAndClear();

            List<Object[]> nearbyTraces = traceRepository.findNearbyTracesLocationsRaw(searchLongitude, searchLatitude,
                    searchDistanceMeters);

            assertThat(nearbyTraces).isEmpty();
        }

        @Test
        void findTracesNearby_shouldReturnTrace_whenTraceInsideRange() {
            double tracaLonitude = TEST_LONGITUDE_1;
            double traceLatitude = TEST_LATITUDE_1;

            User author = TestEntityFactory.createPersistedUser(entityManager);
            TestEntityFactory.createPersistedTrace(entityManager, author, tracaLonitude, traceLatitude);

            double searchLongitude = TEST_LONGITUDE_3;
            double searchLatitude = TEST_LATITUDE_3; // barely inside range
            int searchDistanceMeters = 1000;
            flushAndClear();

            List<Object[]> nearbyTraces = traceRepository.findNearbyTracesLocationsRaw(searchLongitude, searchLatitude,
                    searchDistanceMeters);

            assertThat(nearbyTraces).isNotEmpty().hasSize(1);
        }

        @Test
        void findTracesNearby_shouldReturnMultipleTraces_whenMultipleTracesInRange() {
            double tracaLonitude = TEST_LONGITUDE_1;
            double traceLatitude = TEST_LATITUDE_1;
            User author = TestEntityFactory.createPersistedUser(entityManager);
            Trace trace1 = TestEntityFactory.createPersistedTrace(entityManager, author, tracaLonitude, traceLatitude);

            traceLatitude = TEST_LATITUDE_2;
            tracaLonitude = TEST_LONGITUDE_2;
            Trace trace2 = TestEntityFactory.createPersistedTrace(entityManager, author, tracaLonitude, traceLatitude);
            flushAndClear();

            double searchLongitude = TEST_LONGITUDE_3;
            double searchLatitude = TEST_LATITUDE_3; // should cover both traces
            int searchDistanceMeters = 1000;

            List<Object[]> nearbyTraces = traceRepository.findNearbyTracesLocationsRaw(searchLongitude, searchLatitude,
                    searchDistanceMeters);

            assertThat(nearbyTraces).isNotEmpty().hasSize(2);
            assertThat(nearbyTraces.stream().map(o -> (Long) o[0])).containsExactlyInAnyOrder(trace1.getId(),
                    trace2.getId());
        }
    }
}
