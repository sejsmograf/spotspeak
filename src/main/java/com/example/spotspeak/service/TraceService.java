package com.example.spotspeak.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.repository.TraceRepository;

@Service
public class TraceService {

	private TraceRepository traceRepository;

	public TraceService(TraceRepository traceRepository) {
		this.traceRepository = traceRepository;
	}

	public List<Trace> getAllTraces() {
		return (List<Trace>) traceRepository.findAll();
	}
}
