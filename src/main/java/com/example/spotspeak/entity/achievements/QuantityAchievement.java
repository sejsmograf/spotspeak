package com.example.spotspeak.entity.achievements;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "quantity_achievements")
public class QuantityAchievement extends Achievement {

    @Column(nullable = false)
    private Integer requiredQuantity;
}
