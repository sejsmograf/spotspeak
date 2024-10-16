package com.example.spotspeak.controller;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.service.KeycloakAdminService;
import com.example.spotspeak.service.UserService;

@RestController
@RequestMapping("/account")
public class UserProfileController {
	private KeycloakAdminService keycloakAdminService;
	private UserService userService;

	public UserProfileController(KeycloakAdminService keycloakAdminService,
			UserService userService) {
		this.keycloakAdminService = keycloakAdminService;
		this.userService = userService;
	}

	@PutMapping
	ResponseEntity<User> updateUser(@AuthenticationPrincipal Jwt jwt,
			@RequestBody UserUpdateDTO userUpdateDTO) {
		String userId = jwt.getSubject();
		User user = userService.findById(userId).get();

		keycloakAdminService.updateUser(userId, userUpdateDTO);
		return ResponseEntity.ok(User.builder().build());
	}
}
