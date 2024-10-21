package com.example.spotspeak;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import com.example.spotspeak.entity.Trace;

public class TestDataUtil {
	public static Trace createTrace() {
		GeometryFactory geometryFactory = new GeometryFactory();
		Coordinate coordinate = new Coordinate(1.0, 2.0);

		Trace trace = Trace.builder()
				.id(1L)
				.location(geometryFactory.createPoint(coordinate))
				.build();
		return trace;
	}
}
