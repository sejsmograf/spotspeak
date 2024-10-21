package com.example.spotspeak.service;

import java.util.List;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.*;
import com.example.spotspeak.exception.TraceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.example.spotspeak.repository.TraceRepository;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;

@Service
public class TraceService {

	private TraceRepository traceRepository;
	private GeometryFactory geometryFactory;
	private ResourceService resourceService;
	private TraceTagService traceTagService;
	private S3Service s3Service;

	public TraceService(TraceRepository traceRepository, ResourceService resourceService, TraceTagService traceTagService, S3Service s3Service) {
		this.traceRepository = traceRepository;
		this.geometryFactory = new GeometryFactory();
		this.resourceService = resourceService;
		this.traceTagService = traceTagService;
		this.s3Service = s3Service;
	}

	public List<Trace> getAllTraces() {
		return (List<Trace>) traceRepository.findAll();
	}

	public Trace findTraceById(Long traceId) {
		return traceRepository.findById(traceId).orElseThrow(
				() -> {
					throw new TraceNotFoundException("Could not find the trace");
				});
	}


	public Trace createTrace(TraceUploadDTO traceUploadDTO, User author) {
		Point point = geometryFactory.createPoint(new Coordinate(traceUploadDTO.longitude(), traceUploadDTO.latitude()));

		Resource resource = resourceService.createAndSaveResource(
				traceUploadDTO.keyName(),
				traceUploadDTO.fileName(),
				traceUploadDTO.fileType()
		);

		Trace trace = Trace.builder()
				.location(point)
				.description(traceUploadDTO.description())
				.author(author)
				.resource(resource)
				.isActive(true)
				.build();

		return traceRepository.save(trace);
	}

	@Transactional
	public TraceDownloadDTO getTraceInfo(Long traceId) {
		Trace trace = findTraceById(traceId);
		String keyName = trace.getResource().getS3Key();

		String presignedUrl = s3Service.generatePresignedDownloadUrl(keyName);

		Point location = trace.getLocation();
		Double latitude = location.getY();
		Double longitude = location.getX();

		List<Tag> tags = traceTagService.getTagsForTrace(traceId);
		List<Comment> comments = trace.getComments();

        return new TraceDownloadDTO(
			trace.getId(),
			presignedUrl,
			trace.getDescription(),
			comments,
			tags,
			latitude,
			longitude,
			trace.getAuthor().getFirstName(),
			trace.getAuthor().getLastName(),
			trace.getAuthor().getUsername()
		);
	}
}