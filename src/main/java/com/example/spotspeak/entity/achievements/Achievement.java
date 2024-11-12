package com.example.spotspeak.entity.achievements;

import com.example.spotspeak.entity.Resource;
import com.example.spotspeak.entity.enumeration.EEventType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EEventType eventType;

    @Column(nullable = false)
    @Builder.Default
    private Integer requiredQuantity = 1;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "resource_id", referencedColumnName = "id", nullable = true)
    private Resource iconUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
        name = "achievement_conditions",
        joinColumns = @JoinColumn(name = "achievement_id"),
        inverseJoinColumns = @JoinColumn(name = "condition_id")
    )
    @Builder.Default
    private Set<Condition> conditions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<UserAchievement> userAchievements = new ArrayList<>();
}
