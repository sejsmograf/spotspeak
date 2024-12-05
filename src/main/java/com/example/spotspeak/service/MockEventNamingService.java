package com.example.spotspeak.service;

import java.util.List;
import java.util.Random;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.Trace;

@Service
@Profile({ "test" })
public class MockEventNamingService implements EventNamingService {

    private final String[] eventNames = {
            "Mecz piłki nożnej",
            "Koncert rockowy",
            "Wystawa malarstwa",
            "Festiwal foodtrucków",
            "Pokaz sztucznych ogni",
    };
    private final Random random = new Random();

    @Override
    public String getEventName(List<Trace> associatedTraces) {
        return eventNames[random.nextInt(eventNames.length)];
    }
}
