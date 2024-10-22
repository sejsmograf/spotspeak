package com.example.spotspeak.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserProfileService {
	private UserRepository userRepostitory;
	private ResourceService resourceService;
	private KeycloakClientService keycloakService;

	public UserProfileService(
			UserRepository repository,
			ResourceService resourceService,
			KeycloakClientService keycloakClientService) {

		this.userRepostitory = repository;
		this.resourceService = resourceService;
		this.keycloakService = keycloakClientService;
	}

	@Transactional
	public void deleteById(String userIdString) {
		User user = findByIdOrThrow(userIdString);

		if (user.getProfilePicture() != null) {
			resourceService.deleteResource(user.getProfilePicture().getId());
		}

		userRepostitory.deleteById(user.getId());
		keycloakService.deleteUser(userIdString);
	}

	@Transactional
	public User updateUser(String userIdString, UserUpdateDTO updateDTO) {
		User user = findByIdOrThrow(userIdString);

		if (updateDTO.firstName() != null) {
			user.setFirstName(updateDTO.firstName());
		}
		if (updateDTO.lastName() != null) {
			user.setLastName(updateDTO.lastName());
		}

		userRepostitory.save(user);
		keycloakService.updateUser(userIdString, updateDTO);
		return user;
	}

	public Resource updateUserProfilePicture(String userIdString, MultipartFile file) {
		User user = findByIdOrThrow(userIdString);
		Resource resource = resourceService.uploadUserProfilePicture(userIdString, file);
		user.setProfilePicture(resource);
		return resource;
	}

	private UUID userIdToUUID(String userId) {
		try {
			UUID convertedId = UUID.fromString(userId);
			return convertedId;
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException("Invalid userId format");
		}
	}

	private User findByIdOrThrow(UUID userId) {
		return userRepostitory.findById(userId).orElseThrow(
				() -> {
					throw new UserNotFoundException("Could not find the user");
				});
	}

	public User findByIdOrThrow(String userIdString) {
		UUID convertedId = userIdToUUID(userIdString);
		return findByIdOrThrow(convertedId);
	}
}
