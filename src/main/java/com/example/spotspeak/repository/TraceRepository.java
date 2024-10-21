package com.example.spotspeak.repository;

import org.springframework.data.repository.CrudRepository;
import com.example.spotspeak.entity.Trace;

public interface TraceRepository extends CrudRepository<Trace, Long> {

}

