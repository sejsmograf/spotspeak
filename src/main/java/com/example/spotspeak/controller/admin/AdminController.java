package com.example.spotspeak.controller.admin;

import com.example.spotspeak.service.UserService;
import com.example.spotspeak.service.achievement.AchievementService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private AchievementService achievementService;
    private UserService userService;

    public AdminController(AchievementService achievementService, UserService userService) {
        this.achievementService = achievementService;
        this.userService = userService;
    }

}
