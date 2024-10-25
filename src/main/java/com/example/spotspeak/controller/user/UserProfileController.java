package com.example.spotspeak.controller.user;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.dto.ProfilePictureUpdateDTO;
import com.example.spotspeak.dto.CurrentUserInfoDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.service.UserProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

	private UserProfileService userProfileService;

	public UserProfileController(UserProfileService userService) {
		this.userProfileService = userService;
	}

	@PutMapping
	ResponseEntity<CurrentUserInfoDTO> updateProfile(@AuthenticationPrincipal Jwt jwt,
			@RequestBody UserUpdateDTO userUpdateDTO) {
		String userId = jwt.getSubject();
		userProfileService.updateUser(userId, userUpdateDTO);
		CurrentUserInfoDTO userInfo = userProfileService.getUserInfo(userId);
		return ResponseEntity.ok(userInfo);
	}

	@GetMapping
	ResponseEntity<CurrentUserInfoDTO> getProfile(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		CurrentUserInfoDTO userInfo = userProfileService.getUserInfo(userId);
		return ResponseEntity.ok(userInfo);
	}

	@DeleteMapping
	ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		userProfileService.deleteById(userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/picture")
	ResponseEntity<Resource> updateProfilePicture(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody ProfilePictureUpdateDTO profilePictureUpdateDTO) {
		String userId = jwt.getSubject();
		Resource resource = userProfileService.updateUserProfilePicture(userId,
				profilePictureUpdateDTO.file());
		return ResponseEntity.ok(resource);
	}

	@DeleteMapping("/picture")
	ResponseEntity<Void> deleteProfilePicture(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		userProfileService.deleteUserProfilePicture(userId);
		return ResponseEntity.noContent().build();
	}
}
