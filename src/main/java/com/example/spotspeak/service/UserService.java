package com.example.spotspeak.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.User;
import com.example.spotspeak.repository.UserRepository;

@Service
public class UserService {
	private UserRepository repository;

	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	public Optional<User> findById(UUID userId) {
		return repository.findById(userId);
	}

	public Optional<User> findById(String userId) {
		UUID convertedId = UUID.fromString(userId);
		return repository.findById(convertedId);
	}
}
