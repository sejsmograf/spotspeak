package com.example.spotspeak.controller.ranking;

import com.example.spotspeak.dto.RankingDTO;
import com.example.spotspeak.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public ResponseEntity<List<RankingDTO>> getUserRanking(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<RankingDTO> ranking = rankingService.getUserRanking(userId);
        return ResponseEntity.ok(ranking);
    }
}

