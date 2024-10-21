package com.example.spotspeak.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.exception.UserNotFoundException;
import com.example.spotspeak.repository.UserRepository;

@Service
public class UserService {
	private UserRepository repository;

	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	public User findById(UUID userId) {
		return repository.findById(userId).orElseThrow(
				() -> {
					throw new UserNotFoundException("Could not find the user");
				});
	}

	public User findById(String userIdString) {
		UUID convertedId = UUID.fromString(userIdString);
		return findById(convertedId);
	}

	public void deleteById(String userIdString) {
		try {
			UUID convertedId = UUID.fromString(userIdString);
			repository.deleteById(convertedId);
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException("Invalid or non-existent userId");
		}
	}

	public User updateUser(String userIdString, UserUpdateDTO updateDTO) {
		UUID userId = userIdToUUID(userIdString);
		User user = findById(userId);

		if (updateDTO.firstName() != null) {
			user.setFirstName(updateDTO.firstName());
		}
		if (updateDTO.lastName() != null) {
			user.setLastName(updateDTO.lastName());
		}

		repository.save(user);
		return user;
	}

	private UUID userIdToUUID(String userId) {
		try {
			UUID convertedId = UUID.fromString(userId);
			return convertedId;
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException("Invalid userId format");
		}
	}
}
