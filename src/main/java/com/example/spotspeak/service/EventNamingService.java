package com.example.spotspeak.service;

import java.util.List;

import com.example.spotspeak.entity.Trace;

public interface EventNamingService {

    String getEventName(List<Trace> associatedTraces);
}
