package com.example.spotspeak.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spotspeak.dto.ProfilePictureUpdateDTO;
import com.example.spotspeak.dto.CurrentUserInfoDTO;
import com.example.spotspeak.dto.PasswordUpdateDTO;
import com.example.spotspeak.dto.UserUpdateDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.service.UserProfileService;
import com.example.spotspeak.validation.ValidFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
@Validated
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

	@PutMapping("/update-password")
	ResponseEntity<Void> updatePassword(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
		String userId = jwt.getSubject();
		userProfileService.updateUserPassword(userId, passwordUpdateDTO);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/picture")
	ResponseEntity<Resource> updateProfilePicture(@AuthenticationPrincipal Jwt jwt,
			@Valid @ValidFile(maxSize = 1024 * 1024 * 2, allowedTypes = {
					"image/jpeg", "image/jpg", "image/png"
			}) @RequestPart MultipartFile file) {
		String userId = jwt.getSubject();
		Resource resource = userProfileService.updateUserProfilePicture(userId, file);
		return ResponseEntity.ok(resource);
	}

	@DeleteMapping("/picture")
	ResponseEntity<Void> deleteProfilePicture(@AuthenticationPrincipal Jwt jwt) {
		String userId = jwt.getSubject();
		userProfileService.deleteUserProfilePicture(userId);
		return ResponseEntity.noContent().build();
	}
}
