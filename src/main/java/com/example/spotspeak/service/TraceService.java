package com.example.spotspeak.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
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
	private UserProfileService userProfileService;
	private TagService tagService;

	public TraceService(TraceRepository traceRepository,
			ResourceService resourceService,
			UserProfileService userProfileService,
			TagService tagService) {
		this.traceRepository = traceRepository;
		this.geometryFactory = new GeometryFactory();
		this.resourceService = resourceService;
		this.userProfileService = userProfileService;
		this.tagService = tagService;
	}

	public List<Trace> getAllTraces() {
		return (List<Trace>) traceRepository.findAll();
	}

	public List<Tag> getAllTags() {
		return tagService.getAllTags();
	}

	public List<TraceLocationDTO> getNearbyTraces(double longitude, double latitude, double distance) {
		List<Object[]> results = traceRepository.findNearbyLocations(longitude, latitude, distance);
		return (List<TraceLocationDTO>) results.stream()
				.map(result -> new TraceLocationDTO((Long) result[0], (Double) result[1], (Double) result[2]))
				.collect(Collectors.toList());
	}

	@Transactional
	public Trace createTrace(String userId, MultipartFile file, TraceUploadDTO traceUploadDTO) {
		Point point = geometryFactory
				.createPoint(new Coordinate(traceUploadDTO.longitude(), traceUploadDTO.latitude()));

		User user = userProfileService.findByIdOrThrow(userId);
		Resource resource = file == null
				? null
				: resourceService.uploadTraceResource(userId, file);

		List<Tag> tags = traceUploadDTO.tagIds() == null
				? null
				: tagService.getTagsByIds(traceUploadDTO.tagIds());

		Trace trace = Trace.builder()
				.location(point)
				.description(traceUploadDTO.description())
				.author(user)
				.tags(tags)
				.resource(resource)
				.isActive(true)
				.build();

		return traceRepository.save(trace);
	}

	public void deleteTrace(Long traceId) {
		Trace trace = findByIdOrThrow(traceId);
		Resource resource = trace.getResource();

		if (resource != null) {
			trace.setResource(null);
			resourceService.deleteResource(resource.getId());
		}

		traceRepository.deleteById(traceId);
	}

	@Transactional
	public TraceDownloadDTO getTraceInfo(String userId, String traceId) {
		Trace trace = findByIdOrThrow(traceId);
		userProfileService.findByIdOrThrow(userId); // maybe not necessary

		Resource resource = trace.getResource();

		String presignedUrl = resource == null ? null : resourceService.getResourceAccessUrl(resource.getId());

		return new TraceDownloadDTO(
				trace.getId(),
				presignedUrl,
				trace.getDescription(),
				trace.getComments(),
				trace.getTags(),
				trace.getLocation().getX(),
				trace.getLocation().getY()
		// trace.getAuthor()
		);
	}

	private Trace findByIdOrThrow(Long traceId) {
		return traceRepository.findById(traceId).orElseThrow(
				() -> new TraceNotFoundException("Could not find trace with id: " + traceId));
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
