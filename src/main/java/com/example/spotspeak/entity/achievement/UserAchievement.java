package com.example.spotspeak.entity.achievement;

import com.example.spotspeak.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_achievements")
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "achievement_id", referencedColumnName = "id", nullable = false)
    private Achievement achievement;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantityProgress = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Column(nullable = true)
    private LocalDate lastActionDate;

    @Column(nullable = true)
    private LocalDateTime completedAt;

}
