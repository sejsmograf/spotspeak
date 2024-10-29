package com.example.spotspeak.service;

import java.util.List;
import java.util.UUID;

import com.example.spotspeak.constants.TraceConstants;
import com.example.spotspeak.dto.TraceDownloadDTO;
import com.example.spotspeak.dto.TraceLocationDTO;
import com.example.spotspeak.dto.TraceUploadDTO;
import com.example.spotspeak.entity.*;
import com.example.spotspeak.exception.TraceNotFoundException;
import com.example.spotspeak.exception.TraceNotWithinDistanceException;
import com.example.spotspeak.mapper.TraceMapper;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

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
	private UserService userService;
	private TagService tagService;
	private TraceMapper traceMapper;

	public TraceService(TraceRepository traceRepository,
			ResourceService resourceService,
			UserService userService,
			TagService tagService,
			TraceMapper traceMapper) {
		this.traceRepository = traceRepository;
		this.geometryFactory = new GeometryFactory();
		this.resourceService = resourceService;
		this.userService = userService;
		this.tagService = tagService;
		this.traceMapper = traceMapper;
	}

	public List<TraceDownloadDTO> getTracesForAuthor(String userId) {
		return findTracesWithAuthor(userId).stream()
				.map(traceMapper::createTraceDownloadDTO)
				.toList();
	}

	public List<TraceDownloadDTO> getDiscoveredTraces(String userId) {
		return findDiscoveredTraces(userId).stream()
				.map(traceMapper::createTraceDownloadDTO)
				.toList();
	}

	public List<Trace> getAllTraces() {
		return traceRepository.findAll();
	}

	public List<Tag> getAllTags() {
		return tagService.getAllTags();
	}

	public List<TraceLocationDTO> getNearbyTracesForUser(String userId, double longitude, double latitude,
			double distance) {
		List<Object[]> results = traceRepository.findNearbyTracesLocationsForUser(UUID.fromString(userId),
				longitude, latitude, distance);

		return (List<TraceLocationDTO>) results.stream()
				.map(result -> new TraceLocationDTO((Long) result[0], (Double) result[1], (Double) result[2],
						(boolean) result[3]))
				.toList();
	}

	@Transactional
	public Trace createTrace(String userId, MultipartFile file, TraceUploadDTO traceUploadDTO) {
		User user = userService.findByIdOrThrow(userId);
		Point point = geometryFactory
				.createPoint(new Coordinate(traceUploadDTO.longitude(), traceUploadDTO.latitude()));
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

	public void deleteTrace(Long traceId, String userId) {
		Trace trace = findByIdOrThrow(traceId);
		if (!canUserDeleteTrace(trace, userId)) {
			throw new ForbiddenException("Only author can delete trace");
		}

		Resource resource = trace.getResource();

		if (resource != null) {
			trace.setResource(null);
			resourceService.deleteResource(resource.getId());
		}

		traceRepository.deleteById(traceId);
	}

	public TraceDownloadDTO discoverTrace(String userId,
			Long traceId,
			double longitude,
			double latitude) {

		boolean withinDiscoveryDistance = traceRepository.isTraceWithingDistance(traceId,
				longitude,
				latitude,
				TraceConstants.TRACE_DISCOVERY_DISTANCE);

		if (!withinDiscoveryDistance) {
			throw new TraceNotWithinDistanceException("Trace is not within discovery distance");
		}

		Trace discoveredTrace = findByIdOrThrow(traceId);
		User user = userService.findByIdOrThrow(userId);
		markTraceAsDiscovered(discoveredTrace, user);
		return traceMapper.createTraceDownloadDTO(discoveredTrace);
	}

	public TraceDownloadDTO getTraceInfoForUser(String userId, Long traceId) {
		Trace trace = findByIdOrThrow(traceId);
		User user = userService.findByIdOrThrow(userId);

		if (!hasUserDiscoveredTrace(trace, user)) {
			throw new ForbiddenException("User has not discovered trace");
		}

		return traceMapper.createTraceDownloadDTO(trace);
	}

	private List<Trace> findTracesWithAuthor(String authorId) {
		return traceRepository.findAllByAuthor(UUID.fromString(authorId));
	}

	private List<Trace> findDiscoveredTraces(String authorId) {
		return traceRepository.findDiscoveredTracesByUserId(UUID.fromString(authorId));
	}

	private boolean hasUserDiscoveredTrace(Trace trace, User user) {
		return trace.getDiscoverers().contains(user);
	}

	private void markTraceAsDiscovered(Trace trace, User user) {
		trace.getDiscoverers().add(user);
		user.getDiscoveredTraces().add(trace);
		traceRepository.save(trace);
		userService.saveUser(user);
	}

	private Trace findByIdOrThrow(Long traceId) {
		return traceRepository.findById(traceId).orElseThrow(
				() -> new TraceNotFoundException("Could not find trace with id: " + traceId));
	}

	private boolean canUserDeleteTrace(Trace trace, String userId) {
		return trace.getAuthor().getId().equals(UUID.fromString(userId));
	}
}
