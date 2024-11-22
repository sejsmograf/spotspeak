package com.example.spotspeak.service.achievement;

import com.example.spotspeak.dto.achievement.AchievementUpdateDTO;
import com.example.spotspeak.dto.achievement.AchievementUploadDTO;
import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.entity.achievement.Achievement;
import com.example.spotspeak.entity.achievement.Condition;
import com.example.spotspeak.entity.achievement.UserAchievement;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.example.spotspeak.exception.AchievementExistsException;
import com.example.spotspeak.exception.AchievementNotFoundException;
import com.example.spotspeak.repository.AchievementRepository;
import com.example.spotspeak.repository.ConditionRepository;
import com.example.spotspeak.repository.UserAchievementRepository;
import com.example.spotspeak.service.KeyGenerationService;
import com.example.spotspeak.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AchievementService {

    private AchievementRepository achievementRepository;
    private ConditionRepository conditionRepository;
    private UserAchievementRepository userAchievementRepository;
    private KeyGenerationService keyGenerationService;
    private ResourceService resourceService;
    private Logger logger = LoggerFactory.getLogger(AchievementService.class);

    public AchievementService(AchievementRepository achievementRepository, ConditionRepository conditionRepository,
            UserAchievementRepository userAchievementRepository,
            KeyGenerationService keyGenerationService, ResourceService resourceService) {
        this.achievementRepository = achievementRepository;
        this.conditionRepository = conditionRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.keyGenerationService = keyGenerationService;
        this.resourceService = resourceService;
    }

    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    @Transactional
    public Achievement createAchievement(MultipartFile file, AchievementUploadDTO achievementUploadDTO) {
        Resource resource = file == null ? null : processAndStoreAchievementResource(file);

        if (checkAchievementExists(achievementUploadDTO.name())) {
            throw new AchievementExistsException("Achievement already exists");
        }

        Achievement achievement = Achievement.builder()
                .name(achievementUploadDTO.name())
                .description(achievementUploadDTO.description())
                .points(achievementUploadDTO.points())
                .iconUrl(resource)
                .eventType(EEventType.valueOf(achievementUploadDTO.eventType()))
                .requiredQuantity(achievementUploadDTO.requiredQuantity())
                .createdAt(LocalDateTime.now())
                .build();

        if (achievementUploadDTO.conditions() != null) {
            achievementUploadDTO.conditions().forEach(conditionDTO -> {
                Condition condition = conditionDTO.toCondition();
                conditionRepository.save(condition);
                achievement.getConditions().add(condition);
            });
        }
        achievementRepository.save(achievement);
        return achievement;
    }

    @Transactional
    public Achievement updateAchievement(MultipartFile file, AchievementUpdateDTO achievementUpdateDTO) {
        Achievement achievement = achievementRepository.findById(achievementUpdateDTO.id())
                .orElseThrow(() -> new AchievementNotFoundException("Achievement not found"));

        achievement.setName(achievementUpdateDTO.name());
        achievement.setDescription(achievementUpdateDTO.description());
        achievement.setPoints(achievementUpdateDTO.points());
        achievement.setEventType(EEventType.valueOf(achievementUpdateDTO.eventType()));
        achievement.setRequiredQuantity(achievementUpdateDTO.requiredQuantity());

        if (achievementUpdateDTO.conditions() != null) {
            conditionRepository.deleteAll(achievement.getConditions());
            achievement.getConditions().clear();

            achievementUpdateDTO.conditions().forEach(conditionDTO -> {
                Condition condition = conditionDTO.toCondition();
                conditionRepository.save(condition);
                achievement.getConditions().add(condition);
            });
        }

        if (file != null) {
            updateAchievementIcon(achievement, file);
        }

        return achievementRepository.save(achievement);
    }

    @Transactional
    public void deleteAchievement(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new AchievementNotFoundException("Achievement not found"));

        Resource icon = achievement.getIconUrl();
        if (icon != null) {
            achievement.setIconUrl(null);
            resourceService.deleteResource(icon.getId());
        }

        achievementRepository.delete(achievement);
    }

    private Resource processAndStoreAchievementResource(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String resourceKey = keyGenerationService.generateAchievementIconKey(originalFilename);
        return resourceService.uploadFileAndSaveResource(file, resourceKey);
    }

    private void updateAchievementIcon(Achievement achievement, MultipartFile file) {
        Resource existingIcon = achievement.getIconUrl();
        if (existingIcon != null) {
            achievement.setIconUrl(null);
            resourceService.deleteResource(existingIcon.getId());
        }

        Resource newIcon = processAndStoreAchievementResource(file);
        achievement.setIconUrl(newIcon);
    }

    private boolean checkAchievementExists(String name) {
        return achievementRepository.findByName(name) != null;
    }

    @Transactional
    public void initializeUserAchievements(User user) {
        List<Achievement> allAchievements = getAllAchievements();

        List<UserAchievement> newUserAchievements = allAchievements.stream()
                .filter(achievement -> !userAchievementRepository.existsByUserAndAchievement(user, achievement))
                .map(achievement -> UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .quantityProgress(0)
                        .currentStreak(0)
                        .build())
                .toList();

        userAchievementRepository.saveAll(newUserAchievements);
    }

    @Transactional
    public void initializeAchievementsForAllUsers(List<User> allUsers) {
        if (allUsers == null) {
            allUsers = List.of();
        }
        allUsers.forEach(user -> {
            try {
                initializeUserAchievements(user);
            } catch (Exception e) {
                logger.warn("Failed to initialize achievements for user ID: " + user.getId(), e);
            }
        });
    }

    public Integer getTotalPointsByUser(User user) {
        Integer totalPoints = userAchievementRepository.calculateTotalPointsForUser(user);
        return (totalPoints != null) ? totalPoints : 0;
    }
}
