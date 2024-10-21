package com.example.spotspeak.controller;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.KeycloakClientService;
import com.example.spotspeak.service.S3Service;
import com.example.spotspeak.service.UserService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/account")
public class UserProfileController {

	private KeycloakClientService keycloakClientService;
	private UserService userService;

	public UserProfileController(KeycloakClientService keycloakClientService,
			UserService userService,
			S3Service s3Service) {
		this.keycloakClientService = keycloakClientService;
		this.userService = userService;
	}

	@PutMapping
	@Transactional
	ResponseEntity<User> updateUser(@AuthenticationPrincipal Jwt jwt,
			@RequestBody UserUpdateDTO userUpdateDTO) {
		String userId = jwt.getSubject();
		User user = userService.updateUser(userId, userUpdateDTO);
		keycloakClientService.updateUser(userId, userUpdateDTO);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	ResponseEntity<User> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		User user = userService.findByIdOrThrow(userId);
		return ResponseEntity.ok(user);
	}

	@DeleteMapping
	@Transactional
	ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		userService.deleteById(userId);
		keycloakClientService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/profile-picture")
	@Transactional
	ResponseEntity<Resource> updateProfilePicture(@AuthenticationPrincipal Jwt jwt,
			@RequestParam("file") MultipartFile file) {
		String userId = jwt.getSubject();
		Resource resource = userService.updateUserProfilePicture(userId, file);
		return ResponseEntity.ok(resource);
	}
}
