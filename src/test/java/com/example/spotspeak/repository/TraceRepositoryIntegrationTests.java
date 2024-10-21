package com.example.spotspeak.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.spotspeak.TestDataUtil;
import com.example.spotspeak.entity.Trace;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TraceRepositoryIntegrationTests {

	private TraceRepository traceRepository;

	@Autowired
	public TraceRepositoryIntegrationTests(TraceRepository traceRepository) {
		this.traceRepository = traceRepository;
	}

	@Test
	public void testTraceCanBeCreatedAndRetrieved() {
		Trace validTrace = TestDataUtil.createTrace();
		traceRepository.save(validTrace);

		Optional<Trace> result = traceRepository.findById(validTrace.getId());
		assertThat(result.isPresent()).isTrue();
		assertThat(result.get()).isEqualTo(validTrace);
	}

}
