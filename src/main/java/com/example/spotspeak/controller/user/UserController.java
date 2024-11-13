package com.example.spotspeak.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spotspeak.dto.PublicUserProfileDTO;
import com.example.spotspeak.dto.RegisteredUserDTO;
import com.example.spotspeak.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PublicUserProfileDTO>> searchUsersByUsername(
            @Valid @NotBlank @RequestParam String username) {
        List<PublicUserProfileDTO> users = userService.searchUsersByUsername(username);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/init")
    public ResponseEntity<Void> initializeKeycloakUser(
            @Valid @RequestBody RegisteredUserDTO userDTO) {

        userService.initializeKeycloakUser(userDTO);

        return ResponseEntity.ok().build();
    }
}
