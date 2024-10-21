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
import org.springframework.web.multipart.MultipartFile;

@Service
public class TraceService {

	private TraceRepository traceRepository;
	private GeometryFactory geometryFactory;
	private ResourceService resourceService;
	private TraceTagService traceTagService;
	private UserProfileService userProfileService;
	private S3Service s3Service;

	public TraceService(TraceRepository traceRepository, ResourceService resourceService, TraceTagService traceTagService, UserProfileService userProfileService, S3Service s3Service) {
		this.traceRepository = traceRepository;
		this.geometryFactory = new GeometryFactory();
		this.resourceService = resourceService;
		this.traceTagService = traceTagService;
		this.userProfileService = userProfileService;
		this.s3Service = s3Service;
	}

	public List<Trace> getAllTraces() {
		return (List<Trace>) traceRepository.findAll();
	}

	@Transactional
	public Trace createTrace(String userId, MultipartFile file, TraceUploadDTO traceUploadDTO) {
		Point point = geometryFactory.createPoint(new Coordinate(traceUploadDTO.longitude(), traceUploadDTO.latitude()));

		User user = userProfileService.findByIdOrThrow(userId);
		Resource resource = resourceService.uploadTraceResource(userId, file);

		Trace trace = Trace.builder()
				.location(point)
				.description(traceUploadDTO.description())
				.author(user)
				.resource(resource)
				.isActive(true)
				.build();

		return traceRepository.save(trace);
	}

	@Transactional
	public TraceDownloadDTO getTraceInfo(String userId, String traceId) {
		Trace trace = findByIdOrThrow(traceId);
		userProfileService.findByIdOrThrow(userId); //maybe not necessary

		String keyName = trace.getResource().getS3Key();

		String presignedUrl = s3Service.generatePresignedDownloadUrl(keyName);

        return new TraceDownloadDTO(
			trace.getId(),
			presignedUrl,
			trace.getDescription(),
			trace.getComments(),
			traceTagService.getTagsForTrace(trace.getId()),
			trace.getLocation().getX(),
			trace.getLocation().getY(),
			trace.getAuthor()
		);
	}

	private Trace findByIdOrThrow(Long traceId) {
		return traceRepository.findById(traceId).orElseThrow(
				() -> new TraceNotFoundException("Could not find trace with id: " + traceId)
		);
	}

	private Long traceIdToLong(String traceIdString) {
		try {
			return Long.valueOf(traceIdString);
		} catch (NumberFormatException e) {
			throw new TraceNotFoundException("Invalid traceId format: " + traceIdString);
		}
	}

	public Trace findByIdOrThrow(String traceIdString) {
		Long traceId = traceIdToLong(traceIdString);
		return findByIdOrThrow(traceId);
	}
}