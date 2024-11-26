package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.entity.Event;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

import jakarta.transaction.Transactional;

public class EventServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    private EventService eventService;

    @Test
    @Transactional
    void detectEvents_shouldDetectEvent_whenTracesWithinClusteringDistance() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        double lat = 0.00;
        double lon = 0.00;

        for (int i = 0; i < TraceConstants.EVENT_MIN_POINTS; i++) {
            TestEntityFactory.createPersistedTrace(entityManager, author, null, lon, lat);
        }
        eventService.detectAndCreateEvents();

        List<Event> events = entityManager.createQuery("SELECT e FROM Event e", Event.class).getResultList();
        assertThat(events).isNotEmpty();
    }


    @Test
    @Transactional
    void findEventWithinDistance_shouldReturnNull_whenNoEventWithinDistance() {
        Event found = eventService.findEventWithinDistance(0.00, 0.00, 500);

        assertThat(found).isNull();
    }

    @Test
    @Transactional
    void findEventWithinDistance_shouldReturnEvent_whenWithinDistance() {
        User author = TestEntityFactory.createPersistedUser(entityManager);
        double lat = 0.00;
        double lon = 0.00;
        for (int i = 0; i < TraceConstants.EVENT_MIN_POINTS; i++) {
            TestEntityFactory.createPersistedTrace(entityManager, author, null, lon, lat);
        }
        eventService.detectAndCreateEvents();
        flushAndClear();
        Event found = eventService.findEventWithinDistance(lon, lat, 500);

        assertThat(found).isNotNull();
    }

    @Test
    @Transactional
    void deactivateExpiredEvents_shouldDeactivateEvent_whenExpired() {
        Event event = TestEntityFactory.createPersistedEvent(entityManager);
        event.setExpiresAt(LocalDateTime.now().minusHours(10));
        flushAndClear();

        eventService.deactivateExpiredEvents();
        Event retrieved = entityManager.find(Event.class, event.getId());

        assertThat(retrieved.getIsActive()).isFalse();
    }
}
