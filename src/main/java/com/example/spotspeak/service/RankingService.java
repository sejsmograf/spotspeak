package com.example.spotspeak.service;

import com.example.spotspeak.dto.RankingDTO;
import com.example.spotspeak.entity.User;
import com.example.spotspeak.mapper.RankingMapper;
import com.example.spotspeak.service.achievement.AchievementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private FriendshipService friendshipService;
    private AchievementService achievementService;
    private UserService userService;
    private RankingMapper rankingMapper;

    public RankingService(FriendshipService friendshipService,
                          AchievementService achievementService,
                          UserService userService,
                          RankingMapper rankingMapper) {
        this.friendshipService = friendshipService;
        this.achievementService = achievementService;
        this.userService = userService;
        this.rankingMapper = rankingMapper;
    }

    public List<RankingDTO> getUserRanking(String userId) {
        User user = userService.findByIdOrThrow(userId);
        List<User> friends = friendshipService.getFriends(userId);

        List<RankingDTO> ranking = new ArrayList<>();

        RankingDTO userRankingDTO = rankingMapper.createRankingDTO(user);
        Integer userPoints = achievementService.getTotalPointsByUser(user);
        ranking.add(userRankingDTO.withTotalPoints(userPoints));

        for (User friend : friends) {
            RankingDTO friendRankingDTO = rankingMapper.createRankingDTO(friend);
            Integer friendPoints = achievementService.getTotalPointsByUser(friend);
            ranking.add(friendRankingDTO.withTotalPoints(friendPoints));
        }

        ranking.sort((a, b) -> b.totalPoints().compareTo(a.totalPoints()));

        for (int i = 0; i < ranking.size(); i++) {
            RankingDTO original = ranking.get(i);
            ranking.set(i, original.withRank(i + 1));
        }

        return ranking;
    }
}
