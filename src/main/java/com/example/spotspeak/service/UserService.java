package com.example.spotspeak.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
		return repository.findById(userId).orElseGet(
				() -> {
					throw new UserNotFoundException("Could not find the user");
				});
	}

	public Optional<User> findById(String userId) {
		UUID convertedId = UUID.fromString(userId);
		return repository.findById(convertedId);
	}

	public void deleteById(String userId) {
		try {
			UUID convertedId = UUID.fromString(userId);
			repository.deleteById(convertedId);
		} catch (IllegalArgumentException e) {
			throw new UserNotFoundException("Could not find the user");
		}
	}

}
