package com.example.spotspeak.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import com.example.spotspeak.entity.Trace;

public interface TraceRepository extends CrudRepository<Todo, Long> {

}
