package com.example.spotspeak.controller;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.KeycloakClientService;
import com.example.spotspeak.service.UserService;

@RestController
@RequestMapping("/account")
public class UserProfileController {
	private KeycloakClientService keycloakClientService;
	private UserService userService;

	public UserProfileController(KeycloakClientService keycloakClientService,
			UserService userService) {
		this.keycloakClientService = keycloakClientService;
		this.userService = userService;
	}

	@PutMapping
	ResponseEntity<User> updateUser(@AuthenticationPrincipal Jwt jwt,
			@RequestBody UserUpdateDTO userUpdateDTO) {
		String userId = jwt.getSubject();
		User user = userService.findById(userId).get();

		keycloakClientService.updateUser(userId, userUpdateDTO);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	ResponseEntity<User> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		User user = userService.findById(userId).get();
		return ResponseEntity.ok(user);
	}

	@SuppressWarnings("rawtypes")
	@DeleteMapping
	ResponseEntity deleteUser(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		keycloakClientService.deleteUser(userId);
		userService.deleteById(userId);
		return ResponseEntity.noContent().build();
	}
}
