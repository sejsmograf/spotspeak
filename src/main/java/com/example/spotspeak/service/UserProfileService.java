package com.example.spotspeak.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.UserInfoDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.repository.UserRepository;

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

		try {
			userRepostitory.deleteById(user.getId());
			userRepostitory.flush();
		} catch (DataIntegrityViolationException e) {
			throw new RuntimeException("User deletion failed due to foreign key constraint.", e);
		}

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
		deleteUserProfilePicture(userIdString);

		Resource resource = resourceService.uploadUserProfilePicture(userIdString, file);
		user.setProfilePicture(resource);
		userRepostitory.save(user);
		return resource;
	}

	public void deleteUserProfilePicture(String userId) {
		User user = findByIdOrThrow(userId);
		Resource profilePicture = user.getProfilePicture();

		if (profilePicture != null) {
			user.setProfilePicture(null);
			resourceService.deleteResource(profilePicture.getId());
		}
	}

	public UserInfoDTO getUserInfo(String userId) {
		User user = findByIdOrThrow(userId);
		Resource profilePicture = user.getProfilePicture();
		String profilePictureUrl = profilePicture != null ? resourceService.getResourceAccessUrl(profilePicture.getId())
				: null;

		return new UserInfoDTO(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
				profilePictureUrl);
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
