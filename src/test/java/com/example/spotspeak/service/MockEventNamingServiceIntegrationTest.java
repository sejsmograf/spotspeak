package com.example.spotspeak.service;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.example.spotspeak.TestEntityFactory;
import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.User;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.transaction.Transactional;

public class MockEventNamingServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    private MockEventNamingService eventNamingService;

    @Test
    @Transactional
    void getEventName_shouldReturnEventName() {
        User mockAuthor = TestEntityFactory.createPersistedUser(entityManager);
        List<Trace> associatedTraces = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TestEntityFactory.createPersistedTrace(entityManager, mockAuthor);
        }
        flushAndClear();

        String eventName = eventNamingService.getEventName(associatedTraces);

        assertThat(eventName).isNotBlank();
    }
}
