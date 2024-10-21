package com.example.spotspeak.repository;

import com.example.spotspeak.entity.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Long> {

}
