package com.example.spotspeak;

import com.example.spotspeak.entity.Trace;
import com.example.spotspeak.entity.TraceType;

public class TestDataUtil {
	public static Trace createTrace() {
		Trace trace = Trace.builder()
				.id(1L)
				.latitude(1.0)
				.longitude(1.0)
				.type(TraceType.TEXTONLY)
				.build();
		return trace;
	}
}
