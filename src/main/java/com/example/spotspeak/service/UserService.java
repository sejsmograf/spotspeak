package com.example.spotspeak.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.repository.UserRepository;

@Service
public class UserService {
	private UserRepository userRepostitory;
	private ResourceService resourceService;

	public UserService(UserRepository repository, ResourceService resourceService) {
		this.userRepostitory = repository;
		this.resourceService = resourceService;
	}

	private UUID userIdToUUID(String userId) {
		try {
			UUID convertedId = UUID.fromString(userId);
			return convertedId;
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException("Invalid userId format");
		}
	}

	public User findByIdOrThrow(UUID userId) {
		return userRepostitory.findById(userId).orElseThrow(
				() -> {
					throw new UserNotFoundException("Could not find the user");
				});
	}

	public User findByIdOrThrow(String userIdString) {
		UUID convertedId = userIdToUUID(userIdString);
		return findByIdOrThrow(convertedId);
	}

	public void deleteById(String userIdString) {
		try {
			UUID convertedId = UUID.fromString(userIdString);
			userRepostitory.deleteById(convertedId);
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException("Invalid or non-existent userId");
		}
	}

	public User updateUser(String userIdString, UserUpdateDTO updateDTO) {
		User user = findByIdOrThrow(userIdString);

		if (updateDTO.firstName() != null) {
			user.setFirstName(updateDTO.firstName());
		}
		if (updateDTO.lastName() != null) {
			user.setLastName(updateDTO.lastName());
		}

		userRepostitory.save(user);
		return user;
	}

	public Resource updateUserProfilePicture(String userIdString, MultipartFile file) {
		User user = findByIdOrThrow(userIdString);
		Resource resource = resourceService.uploadUserProfilePicture(userIdString, file);
		user.setProfilePicture(resource);
		return resource;
	}
}
