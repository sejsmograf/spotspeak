package com.example.spotspeak.controller;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
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
import com.example.spotspeak.service.UserProfileService;
import com.example.spotspeak.validation.ValidFile;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/account")
@Validated
public class UserProfileController {

	private UserProfileService userProfileService;

	public UserProfileController(UserProfileService userService) {
		this.userProfileService = userService;
	}

	@PutMapping
	ResponseEntity<User> updateUser(@AuthenticationPrincipal Jwt jwt,
			@RequestBody UserUpdateDTO userUpdateDTO) {
		String userId = jwt.getSubject();
		User user = userProfileService.updateUser(userId, userUpdateDTO);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	ResponseEntity<User> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		User user = userProfileService.findByIdOrThrow(userId);
		return ResponseEntity.ok(user);
	}

	@DeleteMapping
	ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		userProfileService.deleteById(userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/profile-picture")
	@Transactional
	ResponseEntity<Resource> updateProfilePicture(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestParam("file") @ValidFile(maxSize = 1024 * 1024 * 5, allowedTypes = { "image/jpeg",
					"image/jpg", "image/png" }) MultipartFile file) {
		String userId = jwt.getSubject();
		Resource resource = userProfileService.updateUserProfilePicture(userId, file);
		return ResponseEntity.ok(resource);
	}
}
